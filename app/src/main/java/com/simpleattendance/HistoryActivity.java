package com.simpleattendance;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class HistoryActivity extends AppCompatActivity {

    private DatabaseHelper databaseHelper;
    private TextView totalClassesCount, totalStudentsCount;
    private Spinner classFilterSpinner;
    private RecyclerView historyRecyclerView;
    private LinearLayout emptyStateLayout;
    private MaterialButton generateReportButton, exportReportButton;
    
    private List<ClassModel> classList;
    private List<AttendanceSession> sessionList;
    private HistorySessionAdapter historyAdapter;
    private int selectedClassId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        databaseHelper = new DatabaseHelper(this);
        initViews();
        setupToolbar();
        loadData();
        setupSpinner();
        setupRecyclerView();
        updateSummaryStats();
    }

    private void initViews() {
        totalClassesCount = findViewById(R.id.totalClassesCount);
        totalStudentsCount = findViewById(R.id.totalStudentsCount);
        classFilterSpinner = findViewById(R.id.classFilterSpinner);
        historyRecyclerView = findViewById(R.id.historyRecyclerView);
        emptyStateLayout = findViewById(R.id.emptyStateLayout);
        generateReportButton = findViewById(R.id.generateReportButton);
        exportReportButton = findViewById(R.id.exportReportButton);
    }

    private void setupToolbar() {
        ImageView backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> onBackPressed());
    }

    private void loadData() {
        classList = new ArrayList<>();
        sessionList = new ArrayList<>();
        
        // Load all classes
        classList = databaseHelper.getAllClasses();
    }

    private void setupSpinner() {
        List<String> classNames = new ArrayList<>();
        classNames.add("All Classes");
        
        for (ClassModel classModel : classList) {
            String displayName = classModel.getBranch() + " - " + 
                               classModel.getSemester() + " " + classModel.getSection();
            if (classModel.getSubject() != null && !classModel.getSubject().isEmpty()) {
                displayName += " (" + classModel.getSubject() + ")";
            }
            classNames.add(displayName);
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, 
            android.R.layout.simple_spinner_item, classNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        classFilterSpinner.setAdapter(adapter);

        classFilterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    selectedClassId = -1; // All classes
                } else {
                    selectedClassId = classList.get(position - 1).getId();
                }
                loadAttendanceSessions();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void setupRecyclerView() {
        historyAdapter = new HistorySessionAdapter(sessionList);
        historyRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        historyRecyclerView.setAdapter(historyAdapter);
        
        generateReportButton.setOnClickListener(v -> generateReport());
        exportReportButton.setOnClickListener(v -> exportReport());
    }

    private void loadAttendanceSessions() {
        sessionList.clear();
        
        // This is a placeholder implementation
        // In a real app, you would load actual attendance sessions from the database
        // For now, we'll show empty state
        
        if (sessionList.isEmpty()) {
            historyRecyclerView.setVisibility(View.GONE);
            emptyStateLayout.setVisibility(View.VISIBLE);
        } else {
            historyRecyclerView.setVisibility(View.VISIBLE);
            emptyStateLayout.setVisibility(View.GONE);
        }
        
        historyAdapter.notifyDataSetChanged();
    }

    private void updateSummaryStats() {
        totalClassesCount.setText(String.valueOf(classList.size()));
        
        // Count total students across all classes
        int totalStudents = 0;
        for (ClassModel classModel : classList) {
            Cursor studentCursor = databaseHelper.getStudentsCursorByClass(classModel.getId());
            totalStudents += studentCursor.getCount();
            studentCursor.close();
        }
        totalStudentsCount.setText(String.valueOf(totalStudents));
    }

    private void generateReport() {
        if (selectedClassId == -1) {
            Toast.makeText(this, "Please select a specific class to generate report", 
                Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Navigate to reports activity with selected class
        Intent intent = new Intent(this, ReportActivity.class);
        intent.putExtra("classId", selectedClassId);
        startActivity(intent);
    }

    private void exportReport() {
        if (selectedClassId == -1) {
            Toast.makeText(this, "Please select a specific class to export report", 
                Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Implement CSV export functionality
        Toast.makeText(this, "Export functionality coming soon!", 
            Toast.LENGTH_SHORT).show();
    }

    // Inner class for attendance session data (if not already in AttendanceSession.java)
    public static class HistoryAttendanceSession {
        private String className;
        private String subject;
        private String date;
        private int presentCount;
        private int absentCount;
        private int totalStudents;

        public HistoryAttendanceSession(String className, String subject, String date, 
                               int presentCount, int absentCount) {
            this.className = className;
            this.subject = subject;
            this.date = date;
            this.presentCount = presentCount;
            this.absentCount = absentCount;
            this.totalStudents = presentCount + absentCount;
        }

        // Getters and setters
        public String getClassName() { return className; }
        public void setClassName(String className) { this.className = className; }
        
        public String getSubject() { return subject; }
        public void setSubject(String subject) { this.subject = subject; }
        
        public String getDate() { return date; }
        public void setDate(String date) { this.date = date; }
        
        public int getPresentCount() { return presentCount; }
        public void setPresentCount(int presentCount) { this.presentCount = presentCount; }
        
        public int getAbsentCount() { return absentCount; }
        public void setAbsentCount(int absentCount) { this.absentCount = absentCount; }
        
        public int getTotalStudents() { return totalStudents; }
        public void setTotalStudents(int totalStudents) { this.totalStudents = totalStudents; }
        
        public int getAttendancePercentage() {
            if (totalStudents == 0) return 0;
            return Math.round((float) presentCount / totalStudents * 100);
        }
    }
}