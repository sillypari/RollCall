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
    
    private TextView studentNameText, studentEnrollmentText, counterText, classNameText;
    private Button presentButton, absentButton, prevButton, nextButton;
    private Button sortButton, resetButton, finishButton;
    
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
        studentEnrollmentText = findViewById(R.id.studentEnrollmentText);
        counterText = findViewById(R.id.counterText);
        classNameText = findViewById(R.id.classNameText);
        presentButton = findViewById(R.id.presentButton);
        absentButton = findViewById(R.id.absentButton);
        prevButton = findViewById(R.id.prevButton);
        nextButton = findViewById(R.id.nextButton);
        sortButton = findViewById(R.id.sortButton);
        resetButton = findViewById(R.id.resetButton);
        finishButton = findViewById(R.id.finishButton);
        
        databaseHelper = new DatabaseHelper(this);
        attendanceMap = new HashMap<>();
    }

    private void loadData() {
        students = databaseHelper.getStudentsByClass(classId);
        
        classNameText.setText(className);
        
        if (students.isEmpty()) {
            Toast.makeText(this, "No students found for this class", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
    }

    private void finishAttendance() {
        if (attendanceMap.isEmpty()) {
            Toast.makeText(this, "Please mark attendance for at least one student", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show confirmation dialog without subject selection
        showConfirmationDialog();
    }

    private void showConfirmationDialog() {
        int markedCount = attendanceMap.size();
        int totalCount = students.size();
        
        String message = "You have marked " + markedCount + " out of " + totalCount + " students.\n\n" +
                        "Do you want to save the attendance?";

        new AlertDialog.Builder(this)
            .setTitle("Confirm Attendance")
            .setMessage(message)
            .setPositiveButton("Save", (dialog, which) -> saveAttendance())
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void setupClickListeners() {
        presentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Haptic on the actual tapped view
                v.performHapticFeedback(android.view.HapticFeedbackConstants.VIRTUAL_KEY);
                markAttendance("P");
            }
        });

        absentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Haptic on the actual tapped view
                v.performHapticFeedback(android.view.HapticFeedbackConstants.VIRTUAL_KEY);
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

        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetAttendance();
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
            
            // Show brief acknowledgment
            String statusText = "P".equals(status) ? "Present" : "Absent";
            Toast.makeText(this, statusText + " ✓", Toast.LENGTH_SHORT).show();
            
            // Update button colors and student name color to show marked status
            updateButtonColors();
            updateStudentNameColor();

            // Force selected state immediately for smoother visual feedback
            presentButton.setSelected("P".equals(status));
            absentButton.setSelected("A".equals(status));
            
            // Auto-move to next student
            if (currentStudentIndex < students.size() - 1) {
                // Small delay so the selected state (red/green) is visible before advancing
                new android.os.Handler(android.os.Looper.getMainLooper())
                        .postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                moveToNextStudent();
                            }
                        }, 180);
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

    private void resetAttendance() {
        // Show confirmation dialog
        new AlertDialog.Builder(this)
            .setTitle("Reset Attendance")
            .setMessage("Are you sure you want to clear all attendance records? This action cannot be undone.")
            .setPositiveButton("Reset", (dialog, which) -> {
                // Clear all attendance data
                attendanceMap.clear();
                currentStudentIndex = 0;
                updateDisplay();
                Toast.makeText(this, "Attendance reset successfully", Toast.LENGTH_SHORT).show();
            })
            .setNegativeButton("Cancel", null)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .show();
    }

    private void updateDisplay() {
        if (students != null && !students.isEmpty() && currentStudentIndex < students.size()) {
            Student currentStudent = students.get(currentStudentIndex);
            studentNameText.setText(currentStudent.getName());
            studentEnrollmentText.setText(currentStudent.getEnrollmentNumber());
            counterText.setText((currentStudentIndex + 1) + " / " + students.size());
            
            updateButtonColors();
            updateStudentNameColor();
            updateNavigationButtons();
        }
    }

    private void updateButtonColors() {
        // Reset button selection states
        presentButton.setSelected(false);
        absentButton.setSelected(false);
        
        // Highlight current selection
        if (currentStudentIndex < students.size()) {
            Student currentStudent = students.get(currentStudentIndex);
            String status = attendanceMap.get(currentStudent.getId());
            
            if ("P".equals(status)) {
                presentButton.setSelected(true);
            } else if ("A".equals(status)) {
                absentButton.setSelected(true);
            }
        }
    }
    
    private void updateStudentNameColor() {
        if (currentStudentIndex < students.size()) {
            Student currentStudent = students.get(currentStudentIndex);
            String status = attendanceMap.get(currentStudent.getId());
            
            if ("A".equals(status)) {
                // Highlight absent student name in red
                studentNameText.setTextColor(ContextCompat.getColor(this, android.R.color.holo_red_dark));
            } else if ("P".equals(status)) {
                // Highlight present student name in green
                studentNameText.setTextColor(ContextCompat.getColor(this, android.R.color.holo_green_dark));
            } else {
                // Default color for unmarked students
                studentNameText.setTextColor(ContextCompat.getColor(this, android.R.color.black));
            }
        }
    }

    private void updateNavigationButtons() {
        prevButton.setEnabled(currentStudentIndex > 0);
        nextButton.setEnabled(currentStudentIndex < students.size() - 1);
    }

    private void saveAttendance() {
        try {
            String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
            String currentTime = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());
            
            // Create attendance session without subject
            AttendanceSession session = new AttendanceSession(
                classId, -1, currentDate, currentTime, null);
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
                // Generate and show completion screen
                AttendanceReport report = databaseHelper.generateReport((int) sessionId);
                showCompletionScreen(report);
            } else {
                Toast.makeText(this, "Error saving attendance records", Toast.LENGTH_SHORT).show();
            }
            
        } catch (Exception e) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void showCompletionScreen(AttendanceReport report) {
        Intent intent = new Intent(this, AttendanceCompleteActivity.class);
        intent.putExtra("CLASS_NAME", className);
        intent.putExtra("PRESENT_COUNT", report.getPresentCount());
        intent.putExtra("ABSENT_COUNT", report.getAbsentCount());
        intent.putStringArrayListExtra("ABSENT_STUDENTS", new ArrayList<>(report.getAbsentStudents()));
        intent.putStringArrayListExtra("PRESENT_STUDENTS", new ArrayList<>(report.getPresentStudents()));
        startActivity(intent);
        
        Toast.makeText(this, "Attendance saved successfully!", Toast.LENGTH_SHORT).show();
        finish();
    }
}