package com.simpleattendance;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AttendanceActivity extends AppCompatActivity {
    
    private TextView studentNameText, counterText, classNameText;
    private Button presentButton, absentButton, prevButton, nextButton;
    private Button sortButton, finishButton;
    private Spinner subjectSpinner;
    
    private DatabaseHelper databaseHelper;
    private List<Student> students;
    private List<Subject> subjects;
    private Map<Integer, String> attendanceMap; // studentId -> P/A
    private int currentStudentIndex = 0;
    private int classId;
    private String className;
    private boolean isSortedAlphabetically = false;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendance);

        getIntentData();
        initViews();
        loadData();
        setupSpinner();
        setupClickListeners();
        updateDisplay();
    }

    private void getIntentData() {
        classId = getIntent().getIntExtra("CLASS_ID", -1);
        className = getIntent().getStringExtra("CLASS_NAME");
        
        if (classId == -1) {
            Toast.makeText(this, "Error: Invalid class", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
    }

    private void initViews() {
        studentNameText = findViewById(R.id.studentNameText);
        counterText = findViewById(R.id.counterText);
        classNameText = findViewById(R.id.classNameText);
        presentButton = findViewById(R.id.presentButton);
        absentButton = findViewById(R.id.absentButton);
        prevButton = findViewById(R.id.prevButton);
        nextButton = findViewById(R.id.nextButton);
        sortButton = findViewById(R.id.sortButton);
        finishButton = findViewById(R.id.finishButton);
        subjectSpinner = findViewById(R.id.subjectSpinner);
        
        databaseHelper = new DatabaseHelper(this);
        attendanceMap = new HashMap<>();
    }

    private void loadData() {
        students = databaseHelper.getStudentsByClass(classId);
        subjects = databaseHelper.getAllSubjects();
        
        classNameText.setText(className);
        
        if (students.isEmpty()) {
            Toast.makeText(this, "No students found for this class", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
    }

    private void setupSpinner() {
        if (subjects != null && !subjects.isEmpty()) {
            ArrayAdapter<Subject> adapter = new ArrayAdapter<>(this, 
                android.R.layout.simple_spinner_item, subjects);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            subjectSpinner.setAdapter(adapter);
        }
    }

    private void setupClickListeners() {
        presentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                markAttendance("P");
            }
        });

        absentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                markAttendance("A");
            }
        });

        prevButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moveToPreviousStudent();
            }
        });

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moveToNextStudent();
            }
        });

        sortButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleSort();
            }
        });

        finishButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finishAttendance();
            }
        });
    }

    private void markAttendance(String status) {
        if (currentStudentIndex < students.size()) {
            Student currentStudent = students.get(currentStudentIndex);
            attendanceMap.put(currentStudent.getId(), status);
            
            // Update button colors to show marked status
            updateButtonColors();
            
            // Auto-move to next student
            if (currentStudentIndex < students.size() - 1) {
                moveToNextStudent();
            } else {
                Toast.makeText(this, "All students marked! Click Finish to save.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void moveToNextStudent() {
        if (currentStudentIndex < students.size() - 1) {
            currentStudentIndex++;
            updateDisplay();
        }
    }

    private void moveToPreviousStudent() {
        if (currentStudentIndex > 0) {
            currentStudentIndex--;
            updateDisplay();
        }
    }

    private void toggleSort() {
        isSortedAlphabetically = !isSortedAlphabetically;
        
        if (isSortedAlphabetically) {
            students.sort((s1, s2) -> s1.getName().compareToIgnoreCase(s2.getName()));
            sortButton.setText("Sort: Alphabetical");
        } else {
            // Reload original order (CSV order)
            students = databaseHelper.getStudentsByClass(classId);
            sortButton.setText("Sort: CSV Order");
        }
        
        currentStudentIndex = 0;
        updateDisplay();
    }

    private void updateDisplay() {
        if (students != null && !students.isEmpty() && currentStudentIndex < students.size()) {
            Student currentStudent = students.get(currentStudentIndex);
            studentNameText.setText(currentStudent.getDisplayName());
            counterText.setText((currentStudentIndex + 1) + " / " + students.size());
            
            updateButtonColors();
            updateNavigationButtons();
        }
    }

    private void updateButtonColors() {
        // Reset button colors
        presentButton.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, android.R.color.holo_green_light)));
        absentButton.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, android.R.color.holo_red_light)));
        
        // Highlight current selection
        if (currentStudentIndex < students.size()) {
            Student currentStudent = students.get(currentStudentIndex);
            String status = attendanceMap.get(currentStudent.getId());
            
            if ("P".equals(status)) {
                presentButton.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, android.R.color.holo_green_dark)));
            } else if ("A".equals(status)) {
                absentButton.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, android.R.color.holo_red_dark)));
            }
        }
    }

    private void updateNavigationButtons() {
        prevButton.setEnabled(currentStudentIndex > 0);
        nextButton.setEnabled(currentStudentIndex < students.size() - 1);
    }

    private void finishAttendance() {
        if (attendanceMap.isEmpty()) {
            Toast.makeText(this, "Please mark attendance for at least one student", Toast.LENGTH_SHORT).show();
            return;
        }

        Subject selectedSubject = (Subject) subjectSpinner.getSelectedItem();
        if (selectedSubject == null) {
            Toast.makeText(this, "Please select a subject", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show confirmation dialog
        showConfirmationDialog(selectedSubject);
    }

    private void showConfirmationDialog(Subject selectedSubject) {
        int markedCount = attendanceMap.size();
        int totalCount = students.size();
        
        String message = "You have marked " + markedCount + " out of " + totalCount + " students.\n" +
                        "Subject: " + selectedSubject.getName() + "\n\n" +
                        "Do you want to save the attendance?";

        new AlertDialog.Builder(this)
            .setTitle("Confirm Attendance")
            .setMessage(message)
            .setPositiveButton("Save", (dialog, which) -> saveAttendance(selectedSubject))
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void saveAttendance(Subject selectedSubject) {
        try {
            String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
            String currentTime = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());
            
            // Create attendance session
            AttendanceSession session = new AttendanceSession(
                classId, selectedSubject.getId(), currentDate, currentTime, null);
            long sessionId = databaseHelper.insertAttendanceSession(session);
            
            if (sessionId == -1) {
                Toast.makeText(this, "Error saving attendance session", Toast.LENGTH_SHORT).show();
                return;
            }

            // Save attendance records
            int savedCount = 0;
            for (Map.Entry<Integer, String> entry : attendanceMap.entrySet()) {
                AttendanceRecord record = new AttendanceRecord(
                    (int) sessionId, entry.getKey(), entry.getValue());
                long result = databaseHelper.insertAttendanceRecord(record);
                if (result != -1) {
                    savedCount++;
                }
            }

            if (savedCount > 0) {
                // Generate and show report
                AttendanceReport report = databaseHelper.generateReport((int) sessionId);
                showAttendanceReport(report, selectedSubject);
            } else {
                Toast.makeText(this, "Error saving attendance records", Toast.LENGTH_SHORT).show();
            }
            
        } catch (Exception e) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void showAttendanceReport(AttendanceReport report, Subject subject) {
        Intent intent = new Intent(this, ReportActivity.class);
        intent.putExtra("CLASS_NAME", className);
        intent.putExtra("SUBJECT_NAME", subject.getName());
        intent.putExtra("PRESENT_COUNT", report.getPresentCount());
        intent.putExtra("ABSENT_COUNT", report.getAbsentCount());
        intent.putStringArrayListExtra("ABSENT_STUDENTS", new ArrayList<>(report.getAbsentStudents()));
        startActivity(intent);
        
        Toast.makeText(this, "Attendance saved successfully!", Toast.LENGTH_SHORT).show();
        finish();
    }
}