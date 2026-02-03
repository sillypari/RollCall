package com.simpleattendance.ui.createclass

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simpleattendance.data.local.entity.ClassEntity
import com.simpleattendance.data.local.entity.StudentEntity
import com.simpleattendance.data.repository.AttendanceRepository
import com.simpleattendance.util.StudentCsvRow
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CreateClassUiState(
    val branch: String = "",
    val semester: String = "",
    val section: String = "",
    val subject: String = "",
    val students: List<StudentCsvRow> = emptyList(),
    val csvFileName: String? = null,
    val isEditing: Boolean = false,
    val isDuplicating: Boolean = false,
    val editingClassId: Long? = null,
    val isSaving: Boolean = false,
    val isValid: Boolean = false,
    val savedClassId: Long? = null,
    val error: String? = null
)

@HiltViewModel
class CreateClassViewModel @Inject constructor(
    private val repository: AttendanceRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(CreateClassUiState())
    val uiState: StateFlow<CreateClassUiState> = _uiState.asStateFlow()
    
    fun loadClassForEdit(classId: Long) {
        viewModelScope.launch {
            repository.getClassById(classId)?.let { classEntity ->
                val students = repository.getStudentsByClassSync(classId)
                _uiState.value = CreateClassUiState(
                    branch = classEntity.branch,
                    semester = classEntity.semester,
                    section = classEntity.section,
                    subject = classEntity.subject,
                    students = students.map { StudentCsvRow(it.rollNo, it.name) },
                    isEditing = true,
                    editingClassId = classId,
                    isValid = true
                )
            }
        }
    }
    
    fun loadClassForDuplicate(classId: Long) {
        viewModelScope.launch {
            repository.getClassById(classId)?.let { classEntity ->
                val students = repository.getStudentsByClassSync(classId)
                // Pre-fill everything except subject (user will enter new subject)
                _uiState.value = CreateClassUiState(
                    branch = classEntity.branch,
                    semester = classEntity.semester,
                    section = classEntity.section,
                    subject = "", // Empty - user will enter new subject
                    students = students.map { StudentCsvRow(it.rollNo, it.name) },
                    isEditing = false, // Not editing - creating new class
                    isDuplicating = true, // Mark as duplicating
                    editingClassId = null,
                    isValid = false // Not valid yet - needs subject
                )
            }
        }
    }
    
    fun updateBranch(value: String) {
        _uiState.update { it.copy(branch = value) }
        validateForm()
    }
    
    fun updateSemester(value: String) {
        _uiState.update { it.copy(semester = value) }
        validateForm()
    }
    
    fun updateSection(value: String) {
        _uiState.update { it.copy(section = value) }
        validateForm()
    }
    
    fun updateSubject(value: String) {
        _uiState.update { it.copy(subject = value) }
        validateForm()
    }
    
    fun setStudents(students: List<StudentCsvRow>, fileName: String?) {
        _uiState.update { it.copy(students = students, csvFileName = fileName) }
        validateForm()
    }
    
    fun setCsvError(error: String) {
        _uiState.update { it.copy(error = error) }
    }
    
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
    
    private fun validateForm() {
        val state = _uiState.value
        val isValid = state.branch.isNotBlank() &&
                state.semester.isNotBlank() &&
                state.section.isNotBlank() &&
                state.subject.isNotBlank() &&
                state.students.isNotEmpty()
        _uiState.update { it.copy(isValid = isValid) }
    }
    
    fun saveClass() {
        val state = _uiState.value
        if (!state.isValid) return
        
        _uiState.update { it.copy(isSaving = true) }
        
        viewModelScope.launch {
            try {
                val classId = if (state.isEditing && state.editingClassId != null) {
                    // Update existing class
                    val updatedClass = ClassEntity(
                        id = state.editingClassId,
                        branch = state.branch.trim(),
                        semester = state.semester.trim(),
                        section = state.section.trim(),
                        subject = state.subject.trim()
                    )
                    repository.updateClass(updatedClass)
                    repository.deleteStudentsByClass(state.editingClassId)
                    state.editingClassId
                } else {
                    // Create new class
                    val newClass = ClassEntity(
                        branch = state.branch.trim(),
                        semester = state.semester.trim(),
                        section = state.section.trim(),
                        subject = state.subject.trim()
                    )
                    repository.insertClass(newClass)
                }
                
                // Insert students
                val studentEntities = state.students.map { csv ->
                    StudentEntity(
                        classId = classId,
                        rollNo = csv.rollNo,
                        name = csv.name
                    )
                }
                repository.insertStudents(studentEntities)
                
                _uiState.update { it.copy(isSaving = false, savedClassId = classId) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, error = e.message) }
            }
        }
    }
}
