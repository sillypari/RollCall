package com.simpleattendance.ui.createclass

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.simpleattendance.databinding.ActivityCreateClassBinding
import com.simpleattendance.util.CsvParser
import com.simpleattendance.util.HapticUtils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class CreateClassActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityCreateClassBinding
    private val viewModel: CreateClassViewModel by viewModels()
    
    @Inject
    lateinit var hapticUtils: HapticUtils
    
    private val csvPicker = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { handleCsvFile(it) }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateClassBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Check if editing
        intent.getLongExtra("classId", -1L).takeIf { it != -1L }?.let { classId ->
            viewModel.loadClassForEdit(classId)
            binding.toolbar.title = "Edit Class"
            binding.saveButton.text = "Update Class"
        }
        
        // Check if duplicating (pre-fill dept/sem/section but clear subject for new entry)
        intent.getLongExtra("duplicateClassId", -1L).takeIf { it != -1L }?.let { classId ->
            viewModel.loadClassForDuplicate(classId)
            binding.toolbar.title = "New Subject Class"
            binding.saveButton.text = "Create Class"
        }
        
        setupToolbar()
        setupInputFields()
        setupButtons()
        observeState()
    }
    
    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            hapticUtils.lightTap()
            finish()
        }
    }
    
    private fun setupInputFields() {
        binding.branchInput.doAfterTextChanged {
            viewModel.updateBranch(it.toString())
        }
        
        binding.semesterInput.doAfterTextChanged {
            viewModel.updateSemester(it.toString())
        }
        
        binding.sectionInput.doAfterTextChanged {
            viewModel.updateSection(it.toString())
        }
        
        binding.subjectInput.doAfterTextChanged {
            viewModel.updateSubject(it.toString())
        }
    }
    
    private fun setupButtons() {
        binding.selectCsvButton.setOnClickListener {
            hapticUtils.lightTap()
            csvPicker.launch("text/*")
        }
        
        binding.formatInfoButton.setOnClickListener {
            hapticUtils.lightTap()
            showFormatInfo()
        }
        
        binding.saveButton.setOnClickListener {
            hapticUtils.mediumImpact()
            viewModel.saveClass()
        }
    }
    
    private fun observeState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    // Update input fields when editing or duplicating
                    if ((state.isEditing || state.isDuplicating) && binding.branchInput.text.isNullOrEmpty()) {
                        binding.branchInput.setText(state.branch)
                        binding.semesterInput.setText(state.semester)
                        binding.sectionInput.setText(state.section)
                        if (state.isEditing) {
                            binding.subjectInput.setText(state.subject)
                        }
                        // For duplicating, focus on subject input
                        if (state.isDuplicating) {
                            binding.subjectInput.requestFocus()
                        }
                    }
                    
                    // Update CSV status
                    if (state.students.isNotEmpty()) {
                        binding.studentCountText.visibility = View.VISIBLE
                        val statusText = if (state.csvFileName != null) {
                            "${state.students.size} students loaded from ${state.csvFileName}"
                        } else {
                            "${state.students.size} students loaded"
                        }
                        binding.studentCountText.text = statusText
                    }
                    
                    // Update save button
                    binding.saveButton.isEnabled = state.isValid && !state.isSaving
                    
                    // Handle save success
                    state.savedClassId?.let {
                        hapticUtils.successPattern()
                        Toast.makeText(this@CreateClassActivity, "Class saved successfully!", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                    
                    // Handle errors
                    state.error?.let {
                        hapticUtils.errorPattern()
                        Toast.makeText(this@CreateClassActivity, it, Toast.LENGTH_LONG).show()
                        viewModel.clearError()
                    }
                }
            }
        }
    }
    
    private fun handleCsvFile(uri: Uri) {
        val result = CsvParser.parseStudentsCsv(contentResolver, uri)
        result.fold(
            onSuccess = { students ->
                hapticUtils.successPattern()
                val fileName = uri.lastPathSegment ?: "CSV File"
                viewModel.setStudents(students, fileName)
            },
            onFailure = { error ->
                hapticUtils.errorPattern()
                viewModel.setCsvError(error.message ?: "Failed to parse CSV file")
            }
        )
    }
    
    private fun showFormatInfo() {
        MaterialAlertDialogBuilder(this)
            .setTitle("CSV Format")
            .setMessage("""
                Supported formats:
                
                1. Two columns (Roll No, Name):
                   101, John Doe
                   102, Jane Smith
                
                2. Single column (Name only):
                   John Doe
                   Jane Smith
                
                • Header row is automatically detected and skipped
                • Empty rows are ignored
            """.trimIndent())
            .setPositiveButton("Got it", null)
            .show()
    }
}
