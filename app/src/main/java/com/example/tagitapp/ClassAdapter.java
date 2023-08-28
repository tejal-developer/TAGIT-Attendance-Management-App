package com.example.tagitapp;

import static android.content.ContentValues.TAG;
import static com.example.tagitapp.DbHelper.C_ID;
import static com.example.tagitapp.DbHelper.DATE_KEY;
import static com.example.tagitapp.DbHelper.STATUS_KEY;
import static com.example.tagitapp.DbHelper.STATUS_TABLE_NAME;
import static com.example.tagitapp.DbHelper.STUDENT_TABLE_NAME;
import static com.example.tagitapp.DbHelper.S_ID;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Locale;

public class ClassAdapter extends RecyclerView.Adapter<ClassAdapter.ClassViewHolder> {

    ArrayList<ClassItem> classItems;
    Context context;
    private onItemClickListener onItemClickListener;
    private RecyclerView recyclerView;

    public ClassAdapter() {

    }

    public interface onItemClickListener{
        void onClick(int position);
    }

    public void setOnItemClickListener(ClassAdapter.onItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public ClassAdapter(Context context, ArrayList<ClassItem> classItems) {
        this.classItems = classItems;
        this.context=context;
    }
    public static class ClassViewHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener{
        TextView className;
        TextView subjectName;
        Button takeAttendanceButton;
        TextView presentPercentageTextView ;
        TextView absentPercentageTextView ;
        public ClassViewHolder(@NonNull View itemView,onItemClickListener onItemClickListener) {
            super(itemView);
            className=itemView.findViewById(R.id.class_tv);
            subjectName=itemView.findViewById(R.id.subject_tv);
            presentPercentageTextView =itemView.findViewById(R.id.presentPercentageTextView);
            absentPercentageTextView =itemView.findViewById(R.id.absentPercentageTextView);
            takeAttendanceButton = itemView.findViewById(R.id.takeAttendanceButton);

            itemView.setOnClickListener(v->onItemClickListener.onClick(getAdapterPosition()));
            takeAttendanceButton.setOnClickListener(v->onItemClickListener.onClick(getAdapterPosition()));
            itemView.setOnCreateContextMenuListener(this);
        }

        @Override
        public void onCreateContextMenu(ContextMenu contextMenu, View view, ContextMenu.ContextMenuInfo contextMenuInfo) {
            contextMenu.add(getAdapterPosition(),0,0,"EDIT");
            contextMenu.add(getAdapterPosition(),1,0,"DELETE");
        }
    }
    @NonNull
    @Override
    public ClassViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.class_item,parent,false);

        return new ClassViewHolder(itemView,onItemClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ClassViewHolder holder, int position) {
       holder.className.setText(classItems.get(position).getClassName());
       holder.subjectName.setText(classItems.get(position).getSubjectName());

        MyCalender myCalender = new MyCalender();
        String currentDate = myCalender.getDate();
        long classId = classItems.get(position).getCid(); // Replace with the actual method to get the class ID
        calculateAndDisplayAttendancePercentage(currentDate, holder, classId);


    }

    @Override
    public int getItemCount() {
        return classItems.size();
    }


    private void calculateAndDisplayAttendancePercentage(String date, ClassViewHolder viewHolder, long classId) {

        DbHelper dbHelper = new DbHelper(context.getApplicationContext());
        SQLiteDatabase database = dbHelper.getReadableDatabase();

        // Check if attendance data is present for the given date and class ID
        String checkQuery = "SELECT COUNT(*) FROM " + STATUS_TABLE_NAME +
                " WHERE " + DATE_KEY + " = ? AND " + S_ID + " IN (SELECT " + S_ID +
                " FROM " + STUDENT_TABLE_NAME + " WHERE " + C_ID + " = ?)";
        SQLiteStatement checkStatement = database.compileStatement(checkQuery);
        checkStatement.bindString(1, date);
        checkStatement.bindLong(2, classId);
        int dataCount = (int) checkStatement.simpleQueryForLong();

        // Show/hide the "Take Attendance" button and percentage data based on attendance data presence
        if (dataCount == 0) {
            viewHolder.takeAttendanceButton.setVisibility(View.VISIBLE);
            viewHolder.presentPercentageTextView.setVisibility(View.GONE);
            viewHolder.absentPercentageTextView.setVisibility(View.GONE);
        } else {
            viewHolder.takeAttendanceButton.setVisibility(View.GONE);
            viewHolder.presentPercentageTextView.setVisibility(View.VISIBLE);
            viewHolder.absentPercentageTextView.setVisibility(View.VISIBLE);

            // Count students who were present on the given date
            String presentQuery = "SELECT COUNT(*) FROM " + STATUS_TABLE_NAME +
                    " WHERE " + DATE_KEY + " = ? AND " + STATUS_KEY + " = 'P' AND " + S_ID +
                    " IN (SELECT " + S_ID + " FROM " + STUDENT_TABLE_NAME +
                    " WHERE " + C_ID + " = ?)";
            SQLiteStatement presentStatement = database.compileStatement(presentQuery);
            presentStatement.bindString(1, date);
            presentStatement.bindLong(2, classId);
            int presentCount = (int) presentStatement.simpleQueryForLong();

            // Count students who were absent on the given date
            String absentQuery = "SELECT COUNT(*) FROM " + STATUS_TABLE_NAME +
                    " WHERE " + DATE_KEY + " = ? AND " + STATUS_KEY + " = 'A' AND " + S_ID +
                    " IN (SELECT " + S_ID + " FROM " + STUDENT_TABLE_NAME +
                    " WHERE " + C_ID + " = ?)";
            SQLiteStatement absentStatement = database.compileStatement(absentQuery);
            absentStatement.bindString(1, date);
            absentStatement.bindLong(2, classId);
            int absentCount = (int) absentStatement.simpleQueryForLong();

            // Calculate percentages
            int totalCount = presentCount + absentCount;
            double presentPercentage = (double) presentCount / totalCount * 100;
            double absentPercentage = (double) absentCount / totalCount * 100;

            // Display percentages in TextViews
            viewHolder.presentPercentageTextView.setText(String.format(Locale.US, "%.2f%% ", presentPercentage));
            viewHolder.absentPercentageTextView.setText(String.format(Locale.US, "%.2f%% ", absentPercentage));
        }

        database.close();
    }

    public void updatePercentageDataForPosition(int position) {
        if (position >= 0 && position < classItems.size()) {
            ClassItem classItem = classItems.get(position);
            MyCalender myCalender = new MyCalender();
            String currentDate = myCalender.getDate();
            long classId = classItem.getCid();
            // Find the ViewHolder for the specified position
            ClassViewHolder viewHolder = (ClassViewHolder) recyclerView.findViewHolderForAdapterPosition(position);
            if (viewHolder != null) {
                calculateAndDisplayAttendancePercentage(currentDate, viewHolder, classId);
            }
        }
    }

}
