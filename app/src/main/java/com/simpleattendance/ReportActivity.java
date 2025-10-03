package com.simpleattendance;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class ReportActivity extends AppCompatActivity {
    
    private TextView reportTextView;
    private Button shareButton, backButton;
    
    private String className;
    private String subjectName;
    private int presentCount;
    private int absentCount;
    private ArrayList<String> absentStudents;
    private ArrayList<String> presentStudents;
    private SharedPreferences prefs;
    private int reportTemplate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);

        getIntentData();
        initViews();
        generateReport();
        setupClickListeners();
    }

    private void getIntentData() {
        className = getIntent().getStringExtra("CLASS_NAME");
        subjectName = getIntent().getStringExtra("SUBJECT_NAME");
        presentCount = getIntent().getIntExtra("PRESENT_COUNT", 0);
        absentCount = getIntent().getIntExtra("ABSENT_COUNT", 0);
        absentStudents = getIntent().getStringArrayListExtra("ABSENT_STUDENTS");
        presentStudents = getIntent().getStringArrayListExtra("PRESENT_STUDENTS");
        
        if (absentStudents == null) {
            absentStudents = new ArrayList<>();
        }
        if (presentStudents == null) {
            presentStudents = new ArrayList<>();
        }
        
        // Load report template preference
        prefs = getSharedPreferences(SettingsActivity.PREFS_NAME, MODE_PRIVATE);
        reportTemplate = prefs.getInt(SettingsActivity.REPORT_TEMPLATE_KEY, SettingsActivity.TEMPLATE_SHOW_BOTH);
    }

    private void initViews() {
        reportTextView = findViewById(R.id.reportTextView);
        shareButton = findViewById(R.id.shareButton);
        backButton = findViewById(R.id.backButton);
    }

    private void generateReport() {
        String reportText = generateTextReport();
        SpannableStringBuilder spannableBuilder = new SpannableStringBuilder(reportText);
        
        // Determine if we're in dark theme
        boolean isDarkTheme = (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES) ||
                (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM && 
                 (getResources().getConfiguration().uiMode & android.content.res.Configuration.UI_MODE_NIGHT_MASK) 
                 == android.content.res.Configuration.UI_MODE_NIGHT_YES);
        
        // Get colors based on theme
        int presentColor = ContextCompat.getColor(this, 
                isDarkTheme ? R.color.report_present_dark : R.color.report_present_light);
        int absentColor = ContextCompat.getColor(this, 
                isDarkTheme ? R.color.report_absent_dark : R.color.report_absent_light);
        
        // Apply color coding based on template
        switch (reportTemplate) {
            case SettingsActivity.TEMPLATE_SHOW_BOTH:
                // Color present students
                if (!presentStudents.isEmpty()) {
                    int presentHeaderIndex = reportText.indexOf("Present Students:");
                    if (presentHeaderIndex != -1) {
                        for (String student : presentStudents) {
                            int studentIndex = reportText.indexOf(student, presentHeaderIndex);
                            if (studentIndex != -1) {
                                spannableBuilder.setSpan(
                                    new ForegroundColorSpan(presentColor),
                                    studentIndex,
                                    studentIndex + student.length(),
                                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                                );
                            }
                        }
                    }
                }
                
                // Color absent students
                if (!absentStudents.isEmpty()) {
                    int absentHeaderIndex = reportText.indexOf("Absent Students:");
                    if (absentHeaderIndex != -1) {
                        for (String student : absentStudents) {
                            int studentIndex = reportText.indexOf(student, absentHeaderIndex);
                            if (studentIndex != -1) {
                                spannableBuilder.setSpan(
                                    new ForegroundColorSpan(absentColor),
                                    studentIndex,
                                    studentIndex + student.length(),
                                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                                );
                            }
                        }
                    }
                }
                break;
                
            case SettingsActivity.TEMPLATE_SHOW_ABSENT:
                // Color absent students only
                if (!absentStudents.isEmpty()) {
                    int absentHeaderIndex = reportText.indexOf("Absent Students:");
                    if (absentHeaderIndex != -1) {
                        for (String student : absentStudents) {
                            int studentIndex = reportText.indexOf(student, absentHeaderIndex);
                            if (studentIndex != -1) {
                                spannableBuilder.setSpan(
                                    new ForegroundColorSpan(absentColor),
                                    studentIndex,
                                    studentIndex + student.length(),
                                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                                );
                            }
                        }
                    }
                }
                break;
                
            case SettingsActivity.TEMPLATE_SHOW_PRESENT:
                // Color present students only
                if (!presentStudents.isEmpty()) {
                    int presentHeaderIndex = reportText.indexOf("Present Students:");
                    if (presentHeaderIndex != -1) {
                        for (String student : presentStudents) {
                            int studentIndex = reportText.indexOf(student, presentHeaderIndex);
                            if (studentIndex != -1) {
                                spannableBuilder.setSpan(
                                    new ForegroundColorSpan(presentColor),
                                    studentIndex,
                                    studentIndex + student.length(),
                                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                                );
                            }
                        }
                    }
                }
                break;
        }
        
        reportTextView.setText(spannableBuilder);
    }

    private void setupClickListeners() {
        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shareReport();
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private String generateTextReport() {
        StringBuilder sb = new StringBuilder();
        sb.append("ATTENDANCE REPORT\n");
        sb.append("================\n\n");
        sb.append("Class: ").append(className != null ? className : "N/A").append("\n");
        sb.append("Date: ").append(new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date())).append("\n");
        sb.append("Time: ").append(new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date())).append("\n\n");
        
        int totalCount = presentCount + absentCount;
        sb.append("Total Students: ").append(totalCount).append("\n");
        sb.append("Present: ").append(presentCount).append("\n");
        sb.append("Absent: ").append(absentCount).append("\n");
        
        if (totalCount > 0) {
            double attendancePercentage = (double) presentCount / totalCount * 100;
            sb.append("Attendance %: ").append(String.format("%.1f", attendancePercentage)).append("%\n\n");
        }
        
        // Apply report template
        switch (reportTemplate) {
            case SettingsActivity.TEMPLATE_SHOW_ABSENT:
                // Show only absent students
                if (!absentStudents.isEmpty()) {
                    sb.append("Absent Students:\n");
                    sb.append("---------------\n");
                    for (int i = 0; i < absentStudents.size(); i++) {
                        sb.append((i + 1)).append(". ").append(absentStudents.get(i)).append("\n");
                    }
                } else {
                    sb.append("All students were present!\n");
                }
                break;
                
            case SettingsActivity.TEMPLATE_SHOW_PRESENT:
                // Show only present students
                if (!presentStudents.isEmpty()) {
                    sb.append("Present Students:\n");
                    sb.append("----------------\n");
                    for (int i = 0; i < presentStudents.size(); i++) {
                        sb.append((i + 1)).append(". ").append(presentStudents.get(i)).append("\n");
                    }
                } else {
                    sb.append("No students were present.\n");
                }
                break;
                
            case SettingsActivity.TEMPLATE_SHOW_BOTH:
            default:
                // Show both with color coding in TextView
                if (!presentStudents.isEmpty()) {
                    sb.append("Present Students:\n");
                    sb.append("----------------\n");
                    for (int i = 0; i < presentStudents.size(); i++) {
                        sb.append((i + 1)).append(". ").append(presentStudents.get(i)).append("\n");
                    }
                    sb.append("\n");
                }
                
                if (!absentStudents.isEmpty()) {
                    sb.append("Absent Students:\n");
                    sb.append("---------------\n");
                    for (int i = 0; i < absentStudents.size(); i++) {
                        sb.append((i + 1)).append(". ").append(absentStudents.get(i)).append("\n");
                    }
                } else if (presentStudents.isEmpty()) {
                    sb.append("All students were present!\n");
                }
                break;
        }
        
        sb.append("\n").append("Generated by Roll Call by PSB");
        
        return sb.toString();
    }

    private void shareReport() {
        String reportText = generateTextReport();
        
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, reportText);
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Attendance Report - " + className);
        
        try {
            startActivity(Intent.createChooser(shareIntent, "Share Attendance Report"));
        } catch (android.content.ActivityNotFoundException ex) {
            // Handle case where no app can handle the intent
            android.widget.Toast.makeText(this, "No app available to share the report", 
                android.widget.Toast.LENGTH_SHORT).show();
        }
    }
}