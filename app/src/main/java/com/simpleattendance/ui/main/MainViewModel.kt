package com.simpleattendance.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simpleattendance.data.local.entity.ClassEntity
import com.simpleattendance.data.repository.AttendanceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ClassGroup(
    val batchKey: String,
    val displayName: String,
    val classes: List<ClassEntity>,
    val isExpanded: Boolean = false
)

data class MainUiState(
    val classes: List<ClassEntity> = emptyList(),
    val groupedClasses: List<ClassGroup> = emptyList(),
    val isLoading: Boolean = true,
    val isEmpty: Boolean = false
)

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: AttendanceRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()
    
    private val expandedGroups = mutableSetOf<String>()
    
    init {
        loadClasses()
    }
    
    private fun loadClasses() {
        viewModelScope.launch {
            repository.getAllClasses()
                .collect { classes ->
                    val grouped = groupClasses(classes)
                    _uiState.value = MainUiState(
                        classes = classes,
                        groupedClasses = grouped,
                        isLoading = false,
                        isEmpty = classes.isEmpty()
                    )
                }
        }
    }
    
    private fun groupClasses(classes: List<ClassEntity>): List<ClassGroup> {
        return classes
            .groupBy { it.batchKey }
            .map { (key, classList) ->
                val first = classList.first()
                ClassGroup(
                    batchKey = key,
                    displayName = "${first.branch} ${first.semester}-${first.section}",
                    classes = classList.sortedBy { it.subject },
                    isExpanded = expandedGroups.contains(key)
                )
            }
            .sortedBy { it.displayName }
    }
    
    fun toggleGroupExpansion(batchKey: String) {
        if (expandedGroups.contains(batchKey)) {
            expandedGroups.remove(batchKey)
        } else {
            expandedGroups.add(batchKey)
        }
        _uiState.value = _uiState.value.copy(
            groupedClasses = groupClasses(_uiState.value.classes)
        )
    }
    
    fun deleteClass(classEntity: ClassEntity) {
        viewModelScope.launch {
            repository.deleteClass(classEntity)
        }
    }
}
