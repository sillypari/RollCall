package com.simpleattendance;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

public class SettingsActivity extends AppCompatActivity {

    private RadioGroup themeRadioGroup;
    private RadioGroup reportTemplateRadioGroup;
    private RadioButton radioLight, radioDark, radioSystem;
    private RadioButton radioShowBoth, radioShowAbsent, radioShowPresent;
    private Button saveButton;
    private SharedPreferences prefs;

    public static final String PREFS_NAME = "AttendanceAppPrefs";
    public static final String THEME_KEY = "theme_mode";
    public static final String REPORT_TEMPLATE_KEY = "report_template";

    // Theme modes
    public static final int THEME_LIGHT = 0;
    public static final int THEME_DARK = 1;
    public static final int THEME_SYSTEM = 2;

    // Report templates
    public static final int TEMPLATE_SHOW_BOTH = 0;
    public static final int TEMPLATE_SHOW_ABSENT = 1;
    public static final int TEMPLATE_SHOW_PRESENT = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Apply saved theme before calling super.onCreate()
        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        int themeMode = prefs.getInt(THEME_KEY, THEME_SYSTEM);
        applyTheme(themeMode);
        
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Initialize views
        themeRadioGroup = findViewById(R.id.themeRadioGroup);
        reportTemplateRadioGroup = findViewById(R.id.reportTemplateRadioGroup);
        radioLight = findViewById(R.id.radioLight);
        radioDark = findViewById(R.id.radioDark);
        radioSystem = findViewById(R.id.radioSystem);
        radioShowBoth = findViewById(R.id.radioShowBoth);
        radioShowAbsent = findViewById(R.id.radioShowAbsent);
        radioShowPresent = findViewById(R.id.radioShowPresent);
        saveButton = findViewById(R.id.saveButton);

        // Initialize SharedPreferences (already done in onCreate)
        // prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        // Load current settings
        loadSettings();

        // Set up save button
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveSettings();
            }
        });

        // Set up action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Settings");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void loadSettings() {
        // Load theme setting
        int themeMode = prefs.getInt(THEME_KEY, THEME_SYSTEM);
        switch (themeMode) {
            case THEME_LIGHT:
                radioLight.setChecked(true);
                break;
            case THEME_DARK:
                radioDark.setChecked(true);
                break;
            case THEME_SYSTEM:
            default:
                radioSystem.setChecked(true);
                break;
        }

        // Load report template setting
        int reportTemplate = prefs.getInt(REPORT_TEMPLATE_KEY, TEMPLATE_SHOW_BOTH);
        switch (reportTemplate) {
            case TEMPLATE_SHOW_BOTH:
                radioShowBoth.setChecked(true);
                break;
            case TEMPLATE_SHOW_ABSENT:
                radioShowAbsent.setChecked(true);
                break;
            case TEMPLATE_SHOW_PRESENT:
                radioShowPresent.setChecked(true);
                break;
        }
    }

    private void saveSettings() {
        SharedPreferences.Editor editor = prefs.edit();

        // Save theme setting
        int selectedThemeId = themeRadioGroup.getCheckedRadioButtonId();
        int themeMode = THEME_SYSTEM;
        if (selectedThemeId == R.id.radioLight) {
            themeMode = THEME_LIGHT;
        } else if (selectedThemeId == R.id.radioDark) {
            themeMode = THEME_DARK;
        } else if (selectedThemeId == R.id.radioSystem) {
            themeMode = THEME_SYSTEM;
        }
        editor.putInt(THEME_KEY, themeMode);

        // Save report template setting
        int selectedTemplateId = reportTemplateRadioGroup.getCheckedRadioButtonId();
        int reportTemplate = TEMPLATE_SHOW_BOTH;
        if (selectedTemplateId == R.id.radioShowBoth) {
            reportTemplate = TEMPLATE_SHOW_BOTH;
        } else if (selectedTemplateId == R.id.radioShowAbsent) {
            reportTemplate = TEMPLATE_SHOW_ABSENT;
        } else if (selectedTemplateId == R.id.radioShowPresent) {
            reportTemplate = TEMPLATE_SHOW_PRESENT;
        }
        editor.putInt(REPORT_TEMPLATE_KEY, reportTemplate);

        editor.apply();

        // Apply theme immediately
        applyTheme(themeMode);
        
        Toast.makeText(this, "Settings saved successfully", Toast.LENGTH_SHORT).show();
        
        // Go back to trigger recreate on MainActivity
        finish();
    }

    private void applyTheme(int themeMode) {
        switch (themeMode) {
            case THEME_LIGHT:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            case THEME_DARK:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
            case THEME_SYSTEM:
            default:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                break;
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
