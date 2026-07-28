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
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject

data class SessionWithClass(
    val session: AttendanceSessionEntity,
    val classEntity: ClassEntity?
)

data class HistoryUiState(
    val sessions: List<SessionWithClass> = emptyList(),
    val classes: List<ClassEntity> = emptyList(),
    val selectedClassId: Long? = null,
    val searchQuery: String = "",
    val startDate: Long? = null,
    val endDate: Long? = null,
    val isLoading: Boolean = true,
    val isEmpty: Boolean = false,
    val hasAnySessions: Boolean = false,
    val hasActiveFilters: Boolean = false
)

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val repository: AttendanceRepository
) : ViewModel() {
    
    private val _selectedClassId = MutableStateFlow<Long?>(null)
    private val _searchQuery = MutableStateFlow("")
    private val _dateRange = MutableStateFlow<Pair<Long?, Long?>>(null to null)
    
    val uiState: StateFlow<HistoryUiState> = combine(
        repository.getAllClasses(),
        repository.getAllSessions(),
        _selectedClassId,
        _searchQuery,
        _dateRange
    ) { classes, allSessions, selectedId, query, dateRange ->
        val classMap = classes.associateBy { it.id }
        val normalizedQuery = query.trim().lowercase(Locale.getDefault())
        val (startDate, endDate) = dateRange
        val visibleSessions = allSessions.filter { session ->
            val classEntity = classMap[session.classId]
            val matchesClass = selectedId == null || session.classId == selectedId
            val matchesQuery = normalizedQuery.isBlank() || listOfNotNull(
                classEntity?.subject,
                classEntity?.branch,
                classEntity?.section,
                classEntity?.displayName
            ).any { it.lowercase(Locale.getDefault()).contains(normalizedQuery) }
            val matchesStart = startDate == null || session.date >= startDate
            val matchesEnd = endDate == null || session.date <= endDate
            matchesClass && matchesQuery && matchesStart && matchesEnd
        }
        val sessionsWithClass = visibleSessions.map { session ->
            SessionWithClass(
                session = session,
                classEntity = classMap[session.classId]
            )
        }
        HistoryUiState(
            sessions = sessionsWithClass,
            classes = classes,
            selectedClassId = selectedId,
            searchQuery = query,
            startDate = startDate,
            endDate = endDate,
            isLoading = false,
            isEmpty = sessionsWithClass.isEmpty(),
            hasAnySessions = allSessions.isNotEmpty(),
            hasActiveFilters = selectedId != null || normalizedQuery.isNotBlank() ||
                startDate != null || endDate != null
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HistoryUiState()
    )
    
    fun filterByClass(classId: Long?) {
        _selectedClassId.value = classId
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setStartDate(timestamp: Long) {
        val start = boundaryOfDay(timestamp, end = false)
        val currentEnd = _dateRange.value.second
        _dateRange.value = start to if (currentEnd != null && currentEnd < start) {
            boundaryOfDay(timestamp, end = true)
        } else {
            currentEnd
        }
    }

    fun setEndDate(timestamp: Long) {
        val end = boundaryOfDay(timestamp, end = true)
        val currentStart = _dateRange.value.first
        _dateRange.value = if (currentStart != null && currentStart > end) {
            boundaryOfDay(timestamp, end = false) to end
        } else {
            currentStart to end
        }
    }

    fun clearDateRange() {
        _dateRange.value = null to null
    }

    fun clearFilters() {
        _selectedClassId.value = null
        _searchQuery.value = ""
        _dateRange.value = null to null
    }
    
    fun deleteSession(session: AttendanceSessionEntity) {
        viewModelScope.launch {
            repository.deleteSession(session)
        }
    }

    private fun boundaryOfDay(timestamp: Long, end: Boolean): Long {
        return Calendar.getInstance().apply {
            timeInMillis = timestamp
            set(Calendar.HOUR_OF_DAY, if (end) 23 else 0)
            set(Calendar.MINUTE, if (end) 59 else 0)
            set(Calendar.SECOND, if (end) 59 else 0)
            set(Calendar.MILLISECOND, if (end) 999 else 0)
        }.timeInMillis
    }
}
