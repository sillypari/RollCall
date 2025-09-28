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
        historyAdapter = new HistorySessionAdapter(sessionList, new HistorySessionAdapter.OnSessionClickListener() {
            @Override
            public void onSessionClick(AttendanceSession session) {
                openSessionReport(session);
            }
        });
        historyRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        historyRecyclerView.setAdapter(historyAdapter);
    }

    private void loadAttendanceSessions() {
        sessionList.clear();
        
        // Load actual attendance sessions from database
        if (selectedClassId == -1) {
            // Load all attendance sessions
            sessionList.addAll(databaseHelper.getAllAttendanceSessions());
        } else {
            // Load sessions for specific class
            sessionList.addAll(databaseHelper.getAttendanceSessionsByClass(selectedClassId));
        }
        
        if (sessionList.isEmpty()) {
            historyRecyclerView.setVisibility(View.GONE);
            emptyStateLayout.setVisibility(View.VISIBLE);
        } else {
            historyRecyclerView.setVisibility(View.VISIBLE);
            emptyStateLayout.setVisibility(View.GONE);
        }
        
        historyAdapter.notifyDataSetChanged();
        updateSummaryStats();
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

    private void openSessionReport(AttendanceSession session) {
        // Navigate to report activity for this specific session
        AttendanceReport report = databaseHelper.generateReport(session.getId());
        
        Intent intent = new Intent(this, ReportActivity.class);
        intent.putExtra("CLASS_NAME", session.getClassName());
        intent.putExtra("SUBJECT_NAME", "General");
        intent.putExtra("SESSION_DATE", session.getDate());
        intent.putExtra("SESSION_TIME", session.getTime());
        intent.putExtra("PRESENT_COUNT", report.getPresentCount());
        intent.putExtra("ABSENT_COUNT", report.getAbsentCount());
        intent.putStringArrayListExtra("ABSENT_STUDENTS", new ArrayList<>(report.getAbsentStudents()));
        startActivity(intent);
    }
}