package com.example.tagitapp;

import static com.example.tagitapp.DbHelper.C_ID;
import static com.example.tagitapp.DbHelper.DATE_KEY;
import static com.example.tagitapp.DbHelper.STATUS_KEY;
import static com.example.tagitapp.DbHelper.STATUS_TABLE_NAME;
import static com.example.tagitapp.DbHelper.S_ID;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class StudentActivity extends AppCompatActivity {
    private static final int REQUEST_CODE_CSV_PICKER = 101;
    private static final int REQUEST_CODE_FILE_PICKER = 100;
    Toolbar toolbar;
    private String className;
    private String subjectName;
    private long cid;
    private int position;


    private RecyclerView recyclerView;
    private StudentAdapter studentAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private ArrayList<StudentItem> studentItems=new ArrayList<>();
    private TextView subtitle;
    MyCalender myCalender;

    private DbHelper dbHelper;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
         dbHelper=new DbHelper(this);
         myCalender=new MyCalender();
        Intent intent =getIntent();
        className=intent.getStringExtra("className");
        subjectName=intent.getStringExtra("subjectName");
        position=intent.getIntExtra("position",-1);
        cid=intent.getLongExtra("cid",-1);

         subtitle=findViewById(R.id.subtitle_toolbar);
         setToolBar();
         loadData();
         recyclerView=findViewById(R.id.student_recycler);
        recyclerView.setHasFixedSize(true);
        layoutManager=new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        studentAdapter=new StudentAdapter(this,studentItems);
        recyclerView.setAdapter(studentAdapter);
        studentAdapter.setOnItemClickListener(position -> changeStatus(position));
        loadStatusData();
    }

    private void loadData() {
        Cursor cursor=dbHelper.getStudentTable(cid);
        studentItems.clear();
        while(cursor.moveToNext()){
            long sid=cursor.getLong(cursor.getColumnIndex(S_ID));
            int roll=cursor.getInt(cursor.getColumnIndex(DbHelper.STUDENT_ROLL_KEY));
            String name=cursor.getString(cursor.getColumnIndex(DbHelper.STUDENT_NAME_KEY));
            studentItems.add(new StudentItem(sid,roll,name));



        }
        cursor.close();
    }

    private void changeStatus(int position) {
        String status=studentItems.get(position).getStatus();
        if(status.equals("P")) status="A";
        else status="P";
        studentItems.get(position).setStatus(status);
        studentAdapter.notifyItemChanged(position);

    }

    private void setToolBar() {
        toolbar=findViewById(R.id.toolbar);
        TextView title=toolbar.findViewById(R.id.title_toolbar);

        ImageButton back=toolbar.findViewById(R.id.back);
        ImageButton save=toolbar.findViewById(R.id.save);
        save.setOnClickListener(v->saveStatus());

        title.setText(className);
        subtitle.setText(subjectName+" | "+myCalender.getDate());

        back.setOnClickListener(v->onBackPressed());
        toolbar.inflateMenu(R.menu.student_menu);
        toolbar.setOnMenuItemClickListener(menuItem->onMenuItemClick(menuItem));


    }

//    private void saveStatus() {
//        for (StudentItem studentItem:studentItems) {
//           String status=studentItem.getStatus();
//            if(status!="P")status="A";
//            long value= dbHelper.addStatus(studentItem.getSid(),cid,myCalender.getDate(),status);
//            if(value==-1) dbHelper.updateStatus(studentItem.getSid(),myCalender.getDate(),status);
//          Toast.makeText(this, "Attendance Marked !", Toast.LENGTH_SHORT).show();
//        }
//    }

    @SuppressLint("NotifyDataSetChanged")
    private void saveStatus() {
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        String currentDate = myCalender.getDate();
        dbHelper=new DbHelper(this);
        for (StudentItem studentItem : studentItems) {
            String newStatus = studentItem.getStatus();
            if (!newStatus.equals("P")) {
                newStatus = "A";
            }

            String whereClause = DATE_KEY + "='" + currentDate + "' AND " + S_ID + "=" + studentItem.getSid();

            ContentValues values = new ContentValues();
            values.put(C_ID, cid);
            values.put(S_ID, studentItem.getSid());
            values.put(DATE_KEY, currentDate);
            values.put(STATUS_KEY, newStatus);

            int rowsUpdated = database.update(STATUS_TABLE_NAME, values, whereClause, null);

            if (rowsUpdated == 0) {
                long newRowId = database.insert(STATUS_TABLE_NAME, null, values);
                if (newRowId == -1) {
                    // Handle insert failure
                    Toast.makeText(this, "Data failed to Insert !", Toast.LENGTH_SHORT).show();
                }
            }
        }

        database.close();

        Toast.makeText(this, "Attendance Marked!", Toast.LENGTH_SHORT).show();
    }




    private void pickFile() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*"); // All file types
        startActivityForResult(Intent.createChooser(intent, "Select CSV file"), REQUEST_CODE_FILE_PICKER);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_FILE_PICKER && resultCode == RESULT_OK) {
            Uri fileUri = data.getData();
            if (fileUri != null) {
                importStudentsFromCSV(fileUri);
            }
        }
    }

    private void importStudentsFromCSV(Uri csvFileUri) {
        try (InputStream inputStream = getContentResolver().openInputStream(csvFileUri);
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {

            SQLiteDatabase database = dbHelper.getWritableDatabase();
            database.beginTransaction();

            String line;
            while ((line = reader.readLine()) != null) {
                String[] columns = line.split(","); // Assuming CSV format: roll,name

                if (columns.length >= 2) {
                    try {
                        int roll = Integer.parseInt(columns[0]);
                        String name = columns[1];

                        // Add the student to your database
                        long sid = dbHelper.addStudent(cid, roll, name);
                    } catch (NumberFormatException e) {
                        // Handle the case where the roll number is not a valid integer
                        e.printStackTrace(); // Print the exception for debugging
                        // You can also show a user-friendly error message
                        Toast.makeText(this, "Unable to Add Students !", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            database.setTransactionSuccessful();
            database.endTransaction();
            database.close();

            // Refresh your student data after import
            loadData();
            loadStatusData();

            // Notify the user about successful import
            Toast.makeText(this, "Students imported from CSV!", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace(); // Print the exception for debugging
            // Handle the IO exception here, e.g., show an error message to the user
            Toast.makeText(this, "Students imported from CSV!", Toast.LENGTH_SHORT).show();
        }
    }


    private void loadStatusData(){
        for (StudentItem studentItem:studentItems) {
            String status=dbHelper.getStatus(studentItem.getSid(),myCalender.getDate());
            if(status!=null) studentItem.setStatus(status);
            else studentItem.setStatus("");
        }
        studentAdapter.notifyDataSetChanged();
    }
    private boolean onMenuItemClick(MenuItem menuItem) {
        if(menuItem.getItemId()==R.id.add_student){
            showAddStudentDialog();
        } else if(menuItem.getItemId()==R.id.show_calender){
            showCalender();
        }else if(menuItem.getItemId()==R.id.show_attendance_sheet){
            openSheetList();
        }else if(menuItem.getItemId()==R.id.import_from_csv){
            pickFile();
        }
        return true;
    }

    private void openSheetList() {
        long[] idArray=new long[studentItems.size()];
        int[] rollArray=new int[studentItems.size()];
        String[] nameArray=new String[studentItems.size()];
        for(int i=0;i<idArray.length;i++){
            idArray[i]=studentItems.get(i).getSid();
        }
        for(int i=0;i<rollArray.length;i++){
            rollArray[i]=studentItems.get(i).getRoll();
        }
        for(int i=0;i<nameArray.length;i++){
            nameArray[i]=studentItems.get(i).getName();
        }
        Intent intent=new Intent(this,SheetListActivity.class);
        intent.putExtra("cid",cid);
        intent.putExtra("idArray",idArray);
        intent.putExtra("rollArray",rollArray);
        intent.putExtra("nameArray",nameArray);
        intent.putExtra("className",className);
        intent.putExtra("subjectName",subjectName);
        startActivity(intent);

    }

    private void showCalender() {

        myCalender.show(getSupportFragmentManager(),"");
        myCalender.setOnCalendarOkCLickListner(this::onCalenderOkClicked);
    }

    private void onCalenderOkClicked(int year, int month, int day) {
        myCalender.setDate(year,month,day);
        subtitle.setText(subjectName+" | "+myCalender.getDate());
        loadStatusData();
    }

    private void showAddStudentDialog() {
        MyDialog dialog=new MyDialog();
        dialog.show(getSupportFragmentManager(),MyDialog.STUDENT_ADD_DIALOG);
        dialog.setListener((roll,name)->addStudent(roll,name));
    }

    private void addStudent(String roll_string, String name) {
        int roll=Integer.parseInt(roll_string);
        long sid= dbHelper.addStudent(cid,roll,name);
       StudentItem studentItem=new StudentItem(sid,roll,name);
        studentItems.add(studentItem);
        studentAdapter.notifyItemChanged(studentItems.size());
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId()){
            case 0:
                showUpdateStudentDialog(item.getGroupId());
                break;
            case 1:
                deleteStudent(item.getGroupId());
        }
        return super.onContextItemSelected(item);
    }

    private void showUpdateStudentDialog(int position) {
        MyDialog dialog=new MyDialog(studentItems.get(position).getRoll(),studentItems.get(position).getName());
        dialog.show(getSupportFragmentManager(),MyDialog.STUDENT_UPDATE_DIALOG);
        dialog.setListener((roll_string,name)->updateStudent(position,name));
    }
    private void updateStudent(int position,String name){
        dbHelper.updateStudent(studentItems.get(position).getSid(),name);
        studentItems.get(position).setName(name);
        studentAdapter.notifyItemChanged(position);

    }
    private void deleteStudent(int position) {
        dbHelper.deleteStudent(studentItems.get(position).getSid());
        studentItems.remove(position);
        studentAdapter.notifyItemRemoved(position);
    }
}