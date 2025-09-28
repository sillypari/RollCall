package com.simpleattendance;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.widget.LinearLayout;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    
    private RecyclerView classesRecyclerView;
    private LinearLayout addClassButton;
    private ImageView historyButton;
    private ClassAdapter classAdapter;
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        setupRecyclerView();
        loadClasses();
        setupClickListeners();
    }

    private void initViews() {
        classesRecyclerView = findViewById(R.id.classesRecyclerView);
        addClassButton = findViewById(R.id.addClassButton);
        historyButton = findViewById(R.id.historyButton);
        databaseHelper = new DatabaseHelper(this);
    }

    private void setupRecyclerView() {
        classesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    private void loadClasses() {
        List<ClassModel> classList = databaseHelper.getAllClasses();
        classAdapter = new ClassAdapter(classList, new ClassAdapter.OnClassClickListener() {
            @Override
            public void onClassClick(ClassModel classModel) {
                openAttendanceActivity(classModel);
            }

            @Override
            public void onClassLongClick(ClassModel classModel, View anchorView) {
                showClassOptionsMenu(classModel, anchorView);
            }
        });
        classesRecyclerView.setAdapter(classAdapter);
    }

    private void setupClickListeners() {
        addClassButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, CreateClassActivity.class);
                startActivity(intent);
            }
        });

        if (historyButton != null) {
            historyButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(MainActivity.this, HistoryActivity.class);
                    startActivity(intent);
                }
            });
        }
    }

    private void openAttendanceActivity(ClassModel classModel) {
        Intent intent = new Intent(MainActivity.this, AttendanceActivity.class);
        intent.putExtra("CLASS_ID", classModel.getId());
        intent.putExtra("CLASS_NAME", classModel.getDisplayName());
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadClasses(); // Refresh the list when returning from other activities
    }

    private void showClassOptionsMenu(ClassModel classModel, View anchorView) {
        PopupMenu popup = new PopupMenu(this, anchorView);
        popup.getMenuInflater().inflate(R.menu.class_options_menu, popup.getMenu());
        
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getItemId() == R.id.menu_edit_class) {
                    editClass(classModel);
                    return true;
                } else if (item.getItemId() == R.id.menu_delete_class) {
                    confirmDeleteClass(classModel);
                    return true;
                }
                return false;
            }
        });
        
        popup.show();
    }

    private void editClass(ClassModel classModel) {
        Intent intent = new Intent(this, CreateClassActivity.class);
        intent.putExtra("EDIT_MODE", true);
        intent.putExtra("CLASS_ID", classModel.getId());
        intent.putExtra("BRANCH", classModel.getBranch());
        intent.putExtra("SEMESTER", classModel.getSemester());
        intent.putExtra("SECTION", classModel.getSection());
        intent.putExtra("SUBJECT", classModel.getSubject());
        startActivity(intent);
    }

    private void confirmDeleteClass(ClassModel classModel) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Class")
                .setMessage("Are you sure you want to delete \"" + classModel.getDisplayName() + "\"?\n\nThis will also delete all attendance records for this class.")
                .setPositiveButton("Delete", (dialog, which) -> {
                    deleteClass(classModel);
                })
                .setNegativeButton("Cancel", null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void deleteClass(ClassModel classModel) {
        try {
            boolean success = databaseHelper.deleteClass(classModel.getId());
            if (success) {
                Toast.makeText(this, "Class deleted successfully", Toast.LENGTH_SHORT).show();
                loadClasses(); // Refresh the list
            } else {
                Toast.makeText(this, "Failed to delete class", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error deleting class: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}