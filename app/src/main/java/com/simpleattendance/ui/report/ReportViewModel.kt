package com.simpleattendance.ui.report

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simpleattendance.data.local.entity.StudentEntity
import com.simpleattendance.data.repository.AttendanceRepository
import com.simpleattendance.data.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

data class ReportUiState(
    val classDisplayName: String = "",
    val formattedDate: String = "",
    val presentCount: Int = 0,
    val absentCount: Int = 0,
    val totalCount: Int = 0,
    val percentage: Float = 0f,
    val presentStudents: List<StudentEntity> = emptyList(),
    val absentStudents: List<StudentEntity> = emptyList(),
    val reportText: String = "",
    val isLoading: Boolean = true
)

@HiltViewModel
class ReportViewModel @Inject constructor(
    private val repository: AttendanceRepository,
    private val settingsRepository: SettingsRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    
    private val sessionId: Long = savedStateHandle.get<Long>("sessionId") ?: 0L
    
    private val _uiState = MutableStateFlow(ReportUiState())
    val uiState: StateFlow<ReportUiState> = _uiState.asStateFlow()
    
    init {
        loadReport()
    }
    
    private fun loadReport() {
        viewModelScope.launch {
            val session = repository.getSessionById(sessionId) ?: return@launch
            val classEntity = repository.getClassById(session.classId) ?: return@launch
            
            val presentRecords = repository.getPresentRecords(sessionId)
            val absentRecords = repository.getAbsentRecords(sessionId)
            
            val allStudents = repository.getStudentsByClassSync(session.classId)
            val studentMap = allStudents.associateBy { it.id }
            
            val presentStudents = presentRecords.mapNotNull { studentMap[it.studentId] }
            val absentStudents = absentRecords.mapNotNull { studentMap[it.studentId] }
            
            val dateFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
            val formattedDate = dateFormat.format(Date(session.date))
            
            // Get settings for report generation
            val settings = settingsRepository.settings.first()
            
            val reportText = buildReportText(
                className = classEntity.fullDisplayName,
                date = formattedDate,
                allStudents = allStudents,
                presentStudents = presentStudents,
                absentStudents = absentStudents,
                total = session.totalCount,
                percentage = session.percentage,
                template = settings.reportTemplate,
                numberingMode = settings.numberingMode
            )
            
            _uiState.value = ReportUiState(
                classDisplayName = classEntity.fullDisplayName,
                formattedDate = formattedDate,
                presentCount = session.presentCount,
                absentCount = session.absentCount,
                totalCount = session.totalCount,
                percentage = session.percentage,
                presentStudents = presentStudents,
                absentStudents = absentStudents,
                reportText = reportText,
                isLoading = false
            )
        }
    }
    
    private fun buildReportText(
        className: String,
        date: String,
        allStudents: List<StudentEntity>,
        presentStudents: List<StudentEntity>,
        absentStudents: List<StudentEntity>,
        total: Int,
        percentage: Float,
        template: String,
        numberingMode: String
    ): String {
        // Create a map from student ID to their 1-based position in the original list
        val studentPositionMap = allStudents.mapIndexed { index, student -> 
            student.id to (index + 1) 
        }.toMap()
        
        return buildString {
            appendLine("ATTENDANCE REPORT")
            appendLine("=".repeat(30))
            appendLine()
            appendLine("Class: $className")
            appendLine("Date: $date")
            appendLine()
            appendLine("Summary:")
            appendLine("   Present: ${presentStudents.size}")
            appendLine("   Absent: ${absentStudents.size}")
            appendLine("   Total: $total")
            appendLine("   Percentage: ${String.format("%.1f", percentage)}%")
            appendLine()
            
            // Show absent students based on template
            if (template != "present_only" && absentStudents.isNotEmpty()) {
                appendLine("ABSENT STUDENTS:")
                appendLine("-".repeat(20))
                absentStudents.forEachIndexed { index, student ->
                    val number = if (numberingMode == "absolute") {
                        // Use position from original class list
                        "${studentPositionMap[student.id] ?: (index + 1)}"
                    } else {
                        // Relative: sequential numbering 1, 2, 3...
                        "${index + 1}"
                    }
                    appendLine("$number. ${student.name}")
                }
                appendLine()
            }
            
            // Show present students based on template
            if (template != "absent_only" && presentStudents.isNotEmpty()) {
                appendLine("PRESENT STUDENTS:")
                appendLine("-".repeat(20))
                presentStudents.forEachIndexed { index, student ->
                    val number = if (numberingMode == "absolute") {
                        // Use position from original class list
                        "${studentPositionMap[student.id] ?: (index + 1)}"
                    } else {
                        // Relative: sequential numbering 1, 2, 3...
                        "${index + 1}"
                    }
                    appendLine("$number. ${student.name}")
                }
            }
            
            appendLine()
            appendLine("=".repeat(30))
            appendLine("Generated by Roll Call App")
        }
    }
}
