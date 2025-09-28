package com.simpleattendance;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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
    
    private EditText branchInput, semesterInput, sectionInput;
    private Button selectCSVButton, saveClassButton, formatInfoButton;
    private TextView selectedFileText;
    private DatabaseHelper databaseHelper;
    private Uri selectedFileUri;
    
    private ActivityResultLauncher<Intent> filePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_class);

        initViews();
        setupFilePickerLauncher();
        setupClickListeners();
    }

    private void initViews() {
        branchInput = findViewById(R.id.branchInput);
        semesterInput = findViewById(R.id.semesterInput);
        sectionInput = findViewById(R.id.sectionInput);
        selectCSVButton = findViewById(R.id.selectCSVButton);
        saveClassButton = findViewById(R.id.saveClassButton);
        formatInfoButton = findViewById(R.id.formatInfoButton);
        selectedFileText = findViewById(R.id.selectedFileText);
        databaseHelper = new DatabaseHelper(this);
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

        if (branch.isEmpty() || semester.isEmpty() || section.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create class
        String currentDate = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(new Date());
        ClassModel classModel = new ClassModel(branch, semester, section, currentDate);
        
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