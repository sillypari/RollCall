package com.simpleattendance.ui.createclass

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.addCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.simpleattendance.R
import com.simpleattendance.databinding.ActivityCreateClassBinding
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

    private var isPopulatingFields = false
    private var hasUnsavedChanges = false
    
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
        onBackPressedDispatcher.addCallback(this) {
            confirmDiscardChanges()
        }
    }
    
    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            hapticUtils.lightTap()
            confirmDiscardChanges()
        }
    }
    
    private fun setupInputFields() {
        binding.branchInput.doAfterTextChanged {
            if (!isPopulatingFields) hasUnsavedChanges = true
            viewModel.updateBranch(it.toString())
        }
        
        binding.semesterInput.doAfterTextChanged {
            if (!isPopulatingFields) hasUnsavedChanges = true
            viewModel.updateSemester(it.toString())
        }
        
        binding.sectionInput.doAfterTextChanged {
            if (!isPopulatingFields) hasUnsavedChanges = true
            viewModel.updateSection(it.toString())
        }
        
        binding.subjectInput.doAfterTextChanged {
            if (!isPopulatingFields) hasUnsavedChanges = true
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
                        isPopulatingFields = true
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
                        isPopulatingFields = false
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
                    if (binding.saveButton.isEnabled) {
                        binding.saveButton.setBackgroundColor(getColor(R.color.primary))
                        binding.saveButton.setTextColor(getColor(R.color.on_primary))
                        binding.saveButton.alpha = 1.0f
                    } else {
                        binding.saveButton.setBackgroundColor(getColor(R.color.background_tertiary))
                        binding.saveButton.setTextColor(getColor(R.color.text_disabled))
                        binding.saveButton.alpha = 0.5f
                    }
                    
                    // Keep the import action calm and immediately available.
                    binding.csvBorderAnimation.stopAnimation()
                    binding.csvBorderAnimation.visibility = View.GONE
                    
                    // Handle save success
                    state.savedClassId?.let {
                        viewModel.onSavedHandled()
                        hasUnsavedChanges = false
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
        hasUnsavedChanges = true
        // Parsing is done on Dispatchers.IO inside the ViewModel.
        // Haptic and toast feedback is driven by UiState changes observed below.
        viewModel.parseCsvFile(uri, contentResolver)
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

    private fun confirmDiscardChanges() {
        if (!hasUnsavedChanges) {
            finish()
            return
        }

        MaterialAlertDialogBuilder(this)
            .setTitle("Discard changes?")
            .setMessage("Your class details and imported roster have not been saved.")
            .setPositiveButton("Discard") { _, _ -> finish() }
            .setNegativeButton("Keep editing", null)
            .show()
    }
}
