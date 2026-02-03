package com.simpleattendance.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simpleattendance.data.local.entity.AttendanceSessionEntity
import com.simpleattendance.data.local.entity.ClassEntity
import com.simpleattendance.data.repository.AttendanceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SessionWithClass(
    val session: AttendanceSessionEntity,
    val classEntity: ClassEntity?
)

data class HistoryUiState(
    val sessions: List<SessionWithClass> = emptyList(),
    val classes: List<ClassEntity> = emptyList(),
    val selectedClassId: Long? = null,
    val isLoading: Boolean = true,
    val isEmpty: Boolean = false
)

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val repository: AttendanceRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()
    
    private var allClasses: Map<Long, ClassEntity> = emptyMap()
    
    init {
        loadData()
    }
    
    private fun loadData() {
        viewModelScope.launch {
            // First load all classes
            repository.getAllClasses().collect { classes ->
                allClasses = classes.associateBy { it.id }
                _uiState.update { it.copy(classes = classes) }
            }
        }
        
        viewModelScope.launch {
            // Then load sessions
            repository.getAllSessions().collect { sessions ->
                val sessionsWithClass = sessions.map { session ->
                    SessionWithClass(
                        session = session,
                        classEntity = allClasses[session.classId]
                    )
                }
                _uiState.update {
                    it.copy(
                        sessions = sessionsWithClass,
                        isLoading = false,
                        isEmpty = sessionsWithClass.isEmpty()
                    )
                }
            }
        }
    }
    
    fun filterByClass(classId: Long?) {
        _uiState.update { it.copy(selectedClassId = classId) }
        
        viewModelScope.launch {
            val flow = if (classId != null) {
                repository.getSessionsByClass(classId)
            } else {
                repository.getAllSessions()
            }
            
            flow.collect { sessions ->
                val sessionsWithClass = sessions.map { session ->
                    SessionWithClass(
                        session = session,
                        classEntity = allClasses[session.classId]
                    )
                }
                _uiState.update {
                    it.copy(
                        sessions = sessionsWithClass,
                        isEmpty = sessionsWithClass.isEmpty()
                    )
                }
            }
        }
    }
    
    fun deleteSession(session: AttendanceSessionEntity) {
        viewModelScope.launch {
            repository.deleteSession(session)
        }
    }
}
