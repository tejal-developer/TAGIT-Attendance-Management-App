package com.example.tagitapp;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.DecimalFormat;
import java.util.Calendar;

public class SheetActivity extends AppCompatActivity {

    Toolbar toolbar;
    private String className;
    private String subjectName;
    private String month;
    private static final int PERMISSION_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sheet);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        month = getIntent().getStringExtra("month");
        className = getIntent().getStringExtra("className");
        subjectName = getIntent().getStringExtra("subjectName");

        setToolBar();
        showTable();
        FloatingActionButton fabGeneratePdf = findViewById(R.id.generate_pdf_fab);
        fabGeneratePdf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                generatePdf();
            }
        });
    }



    private void showTable() {
        DbHelper dbHelper = new DbHelper(this);
        TableLayout tableLayout = findViewById(R.id.tableLayout);
        long[] idArray = getIntent().getLongArrayExtra("idArray");
        int[] rollArray = getIntent().getIntArrayExtra("rollArray");
        String[] nameArray = getIntent().getStringArrayExtra("nameArray");

        int DAY_IN_MONTH = getDayInMonth(month);

        int rowSize = idArray.length + 1;

        tableLayout.setPadding(16, 16, 16, 16); // Add padding to the entire table

        // College Info Table
        TableLayout collegeInfoTable = new TableLayout(this);

        TableRow collegeRow = new TableRow(this);
        TextView collegeTextView = new TextView(this);
        collegeTextView.setText("Dr Dy Patil College of Engineering and Technology, Pimpri");
        collegeTextView.setTypeface(Typeface.DEFAULT_BOLD);
        collegeTextView.setPadding(16, 16, 16, 16);
        collegeRow.addView(collegeTextView);
        collegeInfoTable.addView(collegeRow);

        TableRow departmentRow = new TableRow(this);
        TextView departmentTextView = new TextView(this);
        departmentTextView.setText("Artificial Intelligence and Data Science");
        departmentTextView.setPadding(16, 16, 16, 16);
        departmentRow.addView(departmentTextView);
        collegeInfoTable.addView(departmentRow);

        TableRow classRow = new TableRow(this);
        TextView classTextView = new TextView(this);
        classTextView.setText("Class: " + className);
        classTextView.setPadding(16, 16, 16, 16);
        classRow.addView(classTextView);
        collegeInfoTable.addView(classRow);

        TableRow subjectRow = new TableRow(this);
        TextView subjectTextView = new TextView(this);
        subjectTextView.setText("Subject: " + subjectName);
        subjectTextView.setPadding(16, 16, 16, 16);
        subjectRow.addView(subjectTextView);
        collegeInfoTable.addView(subjectRow);

        TableRow monthRow = new TableRow(this);
        TextView monthTextView = new TextView(this);
        monthTextView.setText("Month: " + month);
        monthTextView.setPadding(16, 16, 16, 16);
        monthRow.addView(monthTextView);
        collegeInfoTable.addView(monthRow);

        tableLayout.addView(collegeInfoTable);

        // Attendance Table
        TableLayout attendanceTable = new TableLayout(this);

        DecimalFormat percentageFormat = new DecimalFormat("0.00");

        TableRow headerRow = new TableRow(this);
        headerRow.setGravity(Gravity.CENTER);

        TextView rollHeader = createHeaderTextView("Roll");
        headerRow.addView(rollHeader);

        TextView nameHeader = createHeaderTextView("Name");
        headerRow.addView(nameHeader);

        for (int i = 1; i <= DAY_IN_MONTH; i++) {
            TextView dateHeader = createHeaderTextView(String.format("%02d", i));
            headerRow.addView(dateHeader);
        }

        TextView totalLecturesHeader = createHeaderTextView("Total Lectures");
        headerRow.addView(totalLecturesHeader);

        TextView totalPresentHeader = createHeaderTextView("Total Present");
        headerRow.addView(totalPresentHeader);

        TextView percentageHeader = createHeaderTextView("Attendance %");
        headerRow.addView(percentageHeader);

        setRowBorders(headerRow); // Add borders to header row
        attendanceTable.addView(headerRow);

        for (int i = 1; i < rowSize; i++) {
            TableRow dataRow = new TableRow(this);
            dataRow.setBackgroundColor(i % 2 == 0 ? Color.parseColor("#EEEEEE") : Color.parseColor("#E4E4E4"));

            TextView rollTextView = createDataTextView(String.valueOf(rollArray[i - 1]));
            rollTextView.setBackground(null); // Remove background for roll number cell
            dataRow.addView(rollTextView);

            TextView nameTextView = createDataTextView(String.valueOf(nameArray[i - 1]));
            dataRow.addView(nameTextView);

            double totalPresent = 0;
            int totalLecturesForStudent = 0; // Initialize the total lectures for this student

            for (int j = 1; j <= DAY_IN_MONTH; j++) {
                String day = String.format("%02d", j);
                String date = day + "." + month;
                String status = dbHelper.getStatus(idArray[i - 1], date);
                TextView statusTextView = createDataTextView(status);
                dataRow.addView(statusTextView);

                if (status != null && (status.equals("A") || status.equals("P"))) {
                    totalLecturesForStudent++; // Count the lectures for this student if data available
                }

                if (status != null && status.equals("P")) {
                    totalPresent++;
                }
            }

            TextView totalLecturesTextView = createDataTextView(String.valueOf(totalLecturesForStudent));
            dataRow.addView(totalLecturesTextView);

            TextView totalPresentTextView = createDataTextView(String.valueOf(totalPresent));
            dataRow.addView(totalPresentTextView);

            double attendancePercentage = (totalPresent / totalLecturesForStudent) * 100;
            TextView percentageTextView = createDataTextView(percentageFormat.format(attendancePercentage) + "%");
            dataRow.addView(percentageTextView);

            setRowBorders(dataRow);

            attendanceTable.addView(dataRow);
        }

        tableLayout.addView(attendanceTable);

        dbHelper.close();
    }





    private TextView createHeaderTextView(String text) {
        TextView textView = new TextView(this);
        textView.setText(text);
        textView.setTypeface(Typeface.DEFAULT_BOLD);
        textView.setPadding(16, 16, 16, 16);
        textView.setGravity(Gravity.CENTER);
        return textView;
    }

    private TextView createDataTextView(String text) {
        TextView textView = new TextView(this);
        textView.setText(text);
        textView.setPadding(16, 16, 16, 16);
        textView.setGravity(Gravity.CENTER);
        return textView;
    }

    private void setRowBorders(TableRow row) {
        GradientDrawable border = new GradientDrawable();
        border.setColor(Color.WHITE); // Background color of row
        border.setStroke(1, Color.GRAY); // Border color and thickness
        row.setBackground(border);

        for (int i = 0; i < row.getChildCount(); i++) {
            TextView textView = (TextView) row.getChildAt(i);
            textView.setBackgroundResource(R.drawable.row_divider);
        }
    }

    private void setToolBar() {
        toolbar = findViewById(R.id.toolbar);
        TextView title = toolbar.findViewById(R.id.title_toolbar);
        TextView subtitle = toolbar.findViewById(R.id.subtitle_toolbar);
        ImageButton back = toolbar.findViewById(R.id.back);
        ImageButton save = toolbar.findViewById(R.id.save);
        save.setVisibility(View.GONE);

        title.setText(className);
        subtitle.setText(subjectName + " | " + month);

        back.setOnClickListener(v -> onBackPressed());
    }

    private int getDayInMonth(String month) {
        int monthIndex = Integer.parseInt(month.substring(0, 2));
        int year = Integer.parseInt(month.substring(3));

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.MONTH, monthIndex - 1);
        calendar.set(Calendar.YEAR, year);
        return calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                generatePdfFromTableLayout(1);
            } else {
                // Handle permission denied
                Toast.makeText(this, "Permission Denied !", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void generatePdf() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
        } else {
            generatePdfFromTableLayout(1); // Call the method with page number 1
        }
    }

    private void generatePdfFromTableLayout(int pageNumber) {
        TableLayout tableLayout = findViewById(R.id.tableLayout);
        PdfGenerator.generatePdfFromTableLayout(this, tableLayout, className+"_"+subjectName+".pdf",pageNumber,100);
    }

}
