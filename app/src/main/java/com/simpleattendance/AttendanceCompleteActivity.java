package com.simpleattendance;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import com.google.android.material.button.MaterialButton;

public class AttendanceCompleteActivity extends AppCompatActivity {

    private CardView successCard;
    private ImageView checkmarkIcon;
    private TextView completionSummary;
    private MaterialButton viewReportButton, backToHomeButton;
    
    private String className;
    private String subjectName;
    private int presentCount;
    private int absentCount;
    private java.util.ArrayList<String> absentStudents;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendance_complete);

        getIntentData();
        initViews();
        setupClickListeners();
        startSuccessAnimation();
    }

    private void getIntentData() {
        className = getIntent().getStringExtra("CLASS_NAME");
        subjectName = getIntent().getStringExtra("SUBJECT_NAME");
        presentCount = getIntent().getIntExtra("PRESENT_COUNT", 0);
        absentCount = getIntent().getIntExtra("ABSENT_COUNT", 0);
        absentStudents = getIntent().getStringArrayListExtra("ABSENT_STUDENTS");
    }

    private void initViews() {
        successCard = findViewById(R.id.successCard);
        checkmarkIcon = findViewById(R.id.checkmarkIcon);
        completionSummary = findViewById(R.id.completionSummary);
        viewReportButton = findViewById(R.id.viewReportButton);
        backToHomeButton = findViewById(R.id.backToHomeButton);
        
        // Set completion summary
        int totalStudents = presentCount + absentCount;
        completionSummary.setText("Marked attendance for " + totalStudents + " students");
    }

    private void setupClickListeners() {
        viewReportButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openReportActivity();
            }
        });

        backToHomeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goBackToHome();
            }
        });
    }

    private void startSuccessAnimation() {
        // Initial setup
        successCard.setScaleX(0.0f);
        successCard.setScaleY(0.0f);
        checkmarkIcon.setAlpha(0.0f);

        // Scale animation for the card
        successCard.animate()
                .scaleX(1.0f)
                .scaleY(1.0f)
                .setDuration(600)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        // Animate the checkmark with scale and fade
                        checkmarkIcon.setScaleX(0.0f);
                        checkmarkIcon.setScaleY(0.0f);
                        
                        checkmarkIcon.animate()
                                .alpha(1.0f)
                                .scaleX(1.2f)
                                .scaleY(1.2f)
                                .setDuration(300)
                                .setInterpolator(new AccelerateDecelerateInterpolator())
                                .withEndAction(new Runnable() {
                                    @Override
                                    public void run() {
                                        // Scale back to normal size
                                        checkmarkIcon.animate()
                                                .scaleX(1.0f)
                                                .scaleY(1.0f)
                                                .setDuration(200)
                                                .setInterpolator(new AccelerateDecelerateInterpolator())
                                                .start();
                                    }
                                })
                                .start();
                    }
                })
                .start();
    }

    private void openReportActivity() {
        Intent intent = new Intent(this, ReportActivity.class);
        intent.putExtra("CLASS_NAME", className);
        intent.putExtra("SUBJECT_NAME", subjectName);
        intent.putExtra("PRESENT_COUNT", presentCount);
        intent.putExtra("ABSENT_COUNT", absentCount);
        intent.putStringArrayListExtra("ABSENT_STUDENTS", absentStudents);
        startActivity(intent);
        finish();
    }

    private void goBackToHome() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        goBackToHome();
    }
}