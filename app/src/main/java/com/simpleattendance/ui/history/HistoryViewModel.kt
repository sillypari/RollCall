package com.simpleattendance.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simpleattendance.data.local.entity.AttendanceSessionEntity
import com.simpleattendance.data.local.entity.ClassEntity
import com.simpleattendance.data.repository.AttendanceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
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

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val repository: AttendanceRepository
) : ViewModel() {
    
    private val _selectedClassId = MutableStateFlow<Long?>(null)
    
    val uiState: StateFlow<HistoryUiState> = combine(
        repository.getAllClasses(),
        _selectedClassId.flatMapLatest { classId ->
            if (classId != null) {
                repository.getSessionsByClass(classId)
            } else {
                repository.getAllSessions()
            }
        },
        _selectedClassId
    ) { classes, sessions, selectedId ->
        val classMap = classes.associateBy { it.id }
        val sessionsWithClass = sessions.map { session ->
            SessionWithClass(
                session = session,
                classEntity = classMap[session.classId]
            )
        }
        HistoryUiState(
            sessions = sessionsWithClass,
            classes = classes,
            selectedClassId = selectedId,
            isLoading = false,
            isEmpty = sessionsWithClass.isEmpty()
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HistoryUiState()
    )
    
    fun filterByClass(classId: Long?) {
        _selectedClassId.value = classId
    }
    
    fun deleteSession(session: AttendanceSessionEntity) {
        viewModelScope.launch {
            repository.deleteSession(session)
        }
    }
}
