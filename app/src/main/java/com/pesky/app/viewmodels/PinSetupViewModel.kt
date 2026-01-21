package com.pesky.app.viewmodels

import androidx.lifecycle.ViewModel
import com.pesky.app.data.preferences.AppPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

data class PinSetupUiState(
    val isLoading: Boolean = false
)

@HiltViewModel
class PinSetupViewModel @Inject constructor(
    private val appPreferences: AppPreferences
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(PinSetupUiState())
    val uiState: StateFlow<PinSetupUiState> = _uiState.asStateFlow()
    
    /**
     * Set up the quick unlock PIN.
     */
    fun setupPin(pin: String) {
        appPreferences.setupQuickUnlock(pin, "pin")
    }
    
    /**
     * Skip PIN setup.
     */
    fun skipPinSetup() {
        // Just mark that we've been through the setup flow
        // hasDatabase is already set when database was created
    }
}
