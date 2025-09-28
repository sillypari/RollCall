package com.simpleattendance;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CreateClassActivity extends AppCompatActivity {
    
    private EditText branchInput, semesterInput, sectionInput, subjectInput;
    private Button selectCSVButton, saveClassButton, formatInfoButton;
    private TextView selectedFileText;
    private DatabaseHelper databaseHelper;
    private Uri selectedFileUri;
    
    // Edit mode variables
    private boolean isEditMode = false;
    private int editClassId = -1;
    
    private ActivityResultLauncher<Intent> filePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_class);

        checkEditMode();
        initViews();
        setupFilePickerLauncher();
        setupClickListeners();
        setupToolbar();
        
        if (isEditMode) {
            loadClassDataForEditing();
        }
    }

    private void checkEditMode() {
        Intent intent = getIntent();
        isEditMode = intent.getBooleanExtra("EDIT_MODE", false);
        if (isEditMode) {
            editClassId = intent.getIntExtra("CLASS_ID", -1);
        }
    }

    private void initViews() {
        branchInput = findViewById(R.id.branchInput);
        semesterInput = findViewById(R.id.semesterInput);
        sectionInput = findViewById(R.id.sectionInput);
        subjectInput = findViewById(R.id.subjectInput);
        selectCSVButton = findViewById(R.id.selectCSVButton);
        saveClassButton = findViewById(R.id.saveClassButton);
        formatInfoButton = findViewById(R.id.formatInfoButton);
        selectedFileText = findViewById(R.id.selectedFileText);
        databaseHelper = new DatabaseHelper(this);
        
        // Update UI based on mode
        if (isEditMode) {
            saveClassButton.setText("Update Class");
            selectCSVButton.setText("Replace CSV File");
        }
    }

    private void loadClassDataForEditing() {
        Intent intent = getIntent();
        String branch = intent.getStringExtra("BRANCH");
        String semester = intent.getStringExtra("SEMESTER");
        String section = intent.getStringExtra("SECTION");
        String subject = intent.getStringExtra("SUBJECT");
        
        if (branch != null) branchInput.setText(branch);
        if (semester != null) semesterInput.setText(semester);
        if (section != null) sectionInput.setText(section);
        if (subject != null) subjectInput.setText(subject);
        
        // Show current students count
        List<Student> currentStudents = databaseHelper.getStudentsByClass(editClassId);
        if (!currentStudents.isEmpty()) {
            selectedFileText.setText("Current: " + currentStudents.size() + " students loaded");
            selectedFileText.setVisibility(View.VISIBLE);
        }
    }

    private void setupToolbar() {
        ImageView backButton = findViewById(R.id.backButton);
        if (backButton != null) {
            backButton.setOnClickListener(v -> onBackPressed());
        }
    }

    private void setupFilePickerLauncher() {
        filePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null) {
                            selectedFileUri = data.getData();
                            if (selectedFileUri != null) {
                                if (CSVHelper.isValidCSVFormat(selectedFileUri, CreateClassActivity.this)) {
                                    selectedFileText.setText("File selected: " + getFileName(selectedFileUri));
                                    selectedFileText.setVisibility(View.VISIBLE);
                                } else {
                                    Toast.makeText(CreateClassActivity.this, 
                                        "Invalid CSV format. Please check the file.", Toast.LENGTH_LONG).show();
                                    selectedFileUri = null;
                                }
                            }
                        }
                    }
                }
            });
    }

    private void setupClickListeners() {
        selectCSVButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFilePicker();
            }
        });

        saveClassButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveClass();
            }
        });

        formatInfoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFormatInfo();
            }
        });
    }

    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("text/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        try {
            filePickerLauncher.launch(Intent.createChooser(intent, "Select CSV File"));
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(this, "No file manager found. Please install a file manager.", 
                Toast.LENGTH_SHORT).show();
        }
    }

    private void saveClass() {
        String branch = branchInput.getText().toString().trim();
        String semester = semesterInput.getText().toString().trim();
        String section = sectionInput.getText().toString().trim();
        String subject = subjectInput.getText().toString().trim();

        if (branch.isEmpty() || semester.isEmpty() || section.isEmpty()) {
            Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (isEditMode) {
            updateExistingClass(branch, semester, section, subject);
        } else {
            createNewClass(branch, semester, section, subject);
        }
    }

    private void createNewClass(String branch, String semester, String section, String subject) {
        // Create class
        String currentDate = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(new Date());
        ClassModel classModel = new ClassModel(branch, semester, section, subject, currentDate);
        
        long classId = databaseHelper.insertClass(classModel);
        if (classId == -1) {
            Toast.makeText(this, "Class already exists or error occurred", Toast.LENGTH_SHORT).show();
            return;
        }

        // Import students from CSV if file is selected
        if (selectedFileUri != null) {
            importStudentsFromCSV((int) classId);
        }

        Toast.makeText(this, "Class created successfully!", Toast.LENGTH_SHORT).show();
        finish();
    }

    private void updateExistingClass(String branch, String semester, String section, String subject) {
        // Update class information
        ClassModel classModel = databaseHelper.getClassById(editClassId);
        if (classModel == null) {
            Toast.makeText(this, "Error: Class not found", Toast.LENGTH_SHORT).show();
            return;
        }
        
        classModel.setBranch(branch);
        classModel.setSemester(semester);
        classModel.setSection(section);
        classModel.setSubject(subject);
        
        boolean success = databaseHelper.updateClass(classModel);
        if (!success) {
            Toast.makeText(this, "Failed to update class", Toast.LENGTH_SHORT).show();
            return;
        }

        // If new CSV file is selected, replace students
        if (selectedFileUri != null) {
            replaceStudentsFromCSV();
        }

        Toast.makeText(this, "Class updated successfully!", Toast.LENGTH_SHORT).show();
        finish();
    }

    private void replaceStudentsFromCSV() {
        // Delete existing students
        databaseHelper.deleteStudentsByClass(editClassId);
        
        // Import new students from CSV
        importStudentsFromCSV(editClassId);
    }

    private void importStudentsFromCSV(int classId) {
        List<Student> students = CSVHelper.parseCSVFile(selectedFileUri, this, classId);
        
        int successCount = 0;
        for (Student student : students) {
            long result = databaseHelper.insertStudent(student);
            if (result != -1) {
                successCount++;
            }
        }
        
        if (successCount > 0) {
            Toast.makeText(this, successCount + " students imported successfully!", 
                Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "No students were imported. Please check the CSV format.", 
                Toast.LENGTH_LONG).show();
        }
    }

    private void showFormatInfo() {
        new AlertDialog.Builder(this)
            .setTitle("CSV Format Information")
            .setMessage(CSVHelper.getCSVFormatExample())
            .setPositiveButton("OK", null)
            .show();
    }

    private String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            try {
                android.database.Cursor cursor = getContentResolver().query(uri, null, null, null, null);
                if (cursor != null && cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME);
                    if (index >= 0) {
                        result = cursor.getString(index);
                    }
                    cursor.close();
                }
            } catch (Exception e) {
                // Fallback to last path segment
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }
}