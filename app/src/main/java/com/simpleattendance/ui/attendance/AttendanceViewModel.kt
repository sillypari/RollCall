package com.simpleattendance.ui.attendance

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simpleattendance.data.local.entity.AttendanceRecordEntity
import com.simpleattendance.data.local.entity.AttendanceSessionEntity
import com.simpleattendance.data.local.entity.ClassEntity
import com.simpleattendance.data.local.entity.StudentEntity
import com.simpleattendance.data.repository.AttendanceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class StudentAttendance(
    val student: StudentEntity,
    val status: String? = null // null = not marked, "P" = present, "A" = absent
)

data class AttendanceUiState(
    val classEntity: ClassEntity? = null,
    val students: List<StudentAttendance> = emptyList(),
    val currentIndex: Int = 0,
    val isLoading: Boolean = true,
    val isComplete: Boolean = false,
    /** Non-null after a successful save. Consumed by the UI then set back to null. */
    val savedSessionId: Long? = null,
    /** True while a save transaction is in progress. Prevents duplicate saves. */
    val isSaving: Boolean = false,
    val saveError: String? = null,
    val presentCount: Int = 0,
    val absentCount: Int = 0
) {
    val currentStudent: StudentAttendance?
        get() = students.getOrNull(currentIndex)
    
    val progress: Float
        get() = if (students.isEmpty()) 0f else (currentIndex.toFloat() / students.size)
    
    val markedCount: Int
        get() = students.count { it.status != null }
    
    val allMarked: Boolean
        get() = students.isNotEmpty() && students.all { it.status != null }
}

@HiltViewModel
class AttendanceViewModel @Inject constructor(
    private val repository: AttendanceRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    
    private val classId: Long = savedStateHandle.get<Long>("classId") ?: 0L
    
    private val _uiState = MutableStateFlow(AttendanceUiState())
    val uiState: StateFlow<AttendanceUiState> = _uiState.asStateFlow()
    
    init {
        loadClassAndStudents()
    }
    
    private fun loadClassAndStudents() {
        viewModelScope.launch {
            val classEntity = repository.getClassById(classId)
            val students = repository.getStudentsByClassSync(classId)
            
            _uiState.value = AttendanceUiState(
                classEntity = classEntity,
                students = students.map { StudentAttendance(it) },
                isLoading = false
            )
        }
    }
    
    fun markPresent() {
        markAttendance("P")
    }
    
    fun markAbsent() {
        markAttendance("A")
    }
    
    private fun markAttendance(status: String) {
        val state = _uiState.value
        val index = state.currentIndex
        if (index >= state.students.size) return
        
        val updatedStudents = state.students.toMutableList()
        updatedStudents[index] = updatedStudents[index].copy(status = status)
        
        val presentCount = updatedStudents.count { it.status == "P" }
        val absentCount = updatedStudents.count { it.status == "A" }
        
        val newIndex = if (index < state.students.size - 1) index + 1 else index
        val isComplete = updatedStudents.all { it.status != null }
        
        _uiState.value = state.copy(
            students = updatedStudents,
            currentIndex = newIndex,
            presentCount = presentCount,
            absentCount = absentCount,
            isComplete = isComplete
        )
    }
    
    fun goToPrevious() {
        val state = _uiState.value
        if (state.currentIndex > 0) {
            _uiState.value = state.copy(currentIndex = state.currentIndex - 1)
        }
    }
    
    fun goToNext() {
        val state = _uiState.value
        if (state.currentIndex < state.students.size - 1) {
            _uiState.value = state.copy(currentIndex = state.currentIndex + 1)
        }
    }
    
    fun goToStudent(index: Int) {
        val state = _uiState.value
        if (index in state.students.indices) {
            _uiState.value = state.copy(currentIndex = index)
        }
    }
    
    fun resetAttendance() {
        val state = _uiState.value
        val resetStudents = state.students.map { it.copy(status = null) }
        _uiState.value = state.copy(
            students = resetStudents,
            currentIndex = 0,
            presentCount = 0,
            absentCount = 0,
            isComplete = false
        )
    }
    
    fun sortAlphabetically() {
        val state = _uiState.value
        val sortedStudents = state.students.sortedBy { it.student.name.lowercase() }
        _uiState.value = state.copy(
            students = sortedStudents,
            currentIndex = 0
        )
    }
    
    fun sortByOriginalOrder() {
        val state = _uiState.value
        val sortedStudents = state.students.sortedBy { it.student.id }
        _uiState.value = state.copy(
            students = sortedStudents,
            currentIndex = 0
        )
    }
    
    fun saveAttendance() {
        val state = _uiState.value
        // Idempotency guards: prevent duplicate saves from rapid taps
        if (state.students.isEmpty()) return
        if (state.isSaving) return
        if (state.savedSessionId != null) return

        _uiState.update { it.copy(isSaving = true, saveError = null) }

        viewModelScope.launch {
            try {
                // Create session
                val session = AttendanceSessionEntity(
                    classId = classId,
                    presentCount = state.presentCount,
                    absentCount = state.absentCount,
                    totalCount = state.students.size
                )
                val sessionId = repository.insertSession(session)

                // Create records for only marked students
                val records = state.students.mapNotNull { sa ->
                    sa.status?.let { status ->
                        AttendanceRecordEntity(
                            sessionId = sessionId,
                            studentId = sa.student.id,
                            status = status
                        )
                    }
                }
                repository.insertRecords(records)

                // Only update state after full transaction succeeds
                _uiState.update { it.copy(isSaving = false, savedSessionId = sessionId) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, saveError = e.message) }
            }
        }
    }

    /** Call this after the UI has consumed the savedSessionId navigation event. */
    fun onSaveNavigationHandled() {
        _uiState.update { it.copy(savedSessionId = null) }
    }

    /** Call this after the UI has shown the save error. */
    fun onSaveErrorHandled() {
        _uiState.update { it.copy(saveError = null) }
    }
}

