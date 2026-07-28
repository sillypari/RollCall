package com.simpleattendance.ui.settings

import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simpleattendance.data.repository.SettingsRepository
import com.simpleattendance.data.repository.UserSettings
import com.simpleattendance.data.repository.toNightMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {
    
    val settings: StateFlow<UserSettings> = settingsRepository.settings
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UserSettings()
        )
    
    fun setTheme(theme: String) {
        viewModelScope.launch {
            settingsRepository.setTheme(theme)
            applyTheme(theme)
        }
    }
    
    fun setHapticsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setHapticsEnabled(enabled)
        }
    }
    
    fun setNumberingMode(mode: String) {
        viewModelScope.launch {
            settingsRepository.setNumberingMode(mode)
        }
    }
    
    fun setReportTemplate(template: String) {
        viewModelScope.launch {
            settingsRepository.setReportTemplate(template)
        }
    }
    
    fun setAttendanceMode(mode: String) {
        viewModelScope.launch {
            settingsRepository.setAttendanceMode(mode)
        }
    }
    
    private fun applyTheme(theme: String) {
        AppCompatDelegate.setDefaultNightMode(theme.toNightMode())
    }
}
