package com.simpleattendance;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    
    private RecyclerView classesRecyclerView;
    private FloatingActionButton addClassButton;
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
}