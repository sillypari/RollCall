package com.pesky.app.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pesky.app.data.preferences.AppPreferences
import com.pesky.app.data.repository.VaultRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class QuickUnlockUiState(
    val isLoading: Boolean = false,
    val isPinVerified: Boolean = false,
    val quickUnlockEnabled: Boolean = false,
    val databaseName: String = "",
    val pinLength: Int = 4
)

sealed class QuickUnlockEvent {
    object PinVerified : QuickUnlockEvent()
    object DatabaseUnlocked : QuickUnlockEvent()
    object WrongPin : QuickUnlockEvent()
    object WrongPassword : QuickUnlockEvent()
    data class Error(val message: String) : QuickUnlockEvent()
}

@HiltViewModel
class QuickUnlockViewModel @Inject constructor(
    private val appPreferences: AppPreferences,
    private val vaultRepository: VaultRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(QuickUnlockUiState())
    val uiState: StateFlow<QuickUnlockUiState> = _uiState.asStateFlow()
    
    private val _events = MutableSharedFlow<QuickUnlockEvent>()
    val events: SharedFlow<QuickUnlockEvent> = _events.asSharedFlow()
    
    init {
        _uiState.value = QuickUnlockUiState(
            quickUnlockEnabled = appPreferences.quickUnlockEnabled,
            databaseName = appPreferences.databaseName,
            // If no quick unlock, go straight to master password
            isPinVerified = !appPreferences.quickUnlockEnabled,
            pinLength = appPreferences.quickUnlockPinLength
        )
    }
    
    /**
     * Verify the PIN.
     */
    fun verifyPin(pin: String) {
        if (appPreferences.verifyQuickUnlock(pin)) {
            _uiState.value = _uiState.value.copy(isPinVerified = true)
            viewModelScope.launch {
                _events.emit(QuickUnlockEvent.PinVerified)
            }
        } else {
            viewModelScope.launch {
                _events.emit(QuickUnlockEvent.WrongPin)
            }
        }
    }
    
    /**
     * Unlock the database with master password.
     */
    fun unlockDatabase(masterPassword: CharArray) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            val result = vaultRepository.openDatabaseFromInternal(masterPassword)
            
            _uiState.value = _uiState.value.copy(isLoading = false)
            
            result.fold(
                onSuccess = {
                    _events.emit(QuickUnlockEvent.DatabaseUnlocked)
                },
                onFailure = { error ->
                    if (error.message?.contains("password", ignoreCase = true) == true) {
                        _events.emit(QuickUnlockEvent.WrongPassword)
                    } else {
                        _events.emit(QuickUnlockEvent.Error(error.message ?: "Failed to unlock"))
                    }
                }
            )
        }
    }
}
