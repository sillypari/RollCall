package com.pesky.app.viewmodels

import android.content.Context
import android.net.Uri
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pesky.app.data.repository.VaultRepository
import com.pesky.app.utils.BiometricAvailability
import com.pesky.app.utils.BiometricHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "pesky_settings")

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: VaultRepository,
    private val biometricHelper: BiometricHelper
) : ViewModel() {
    
    companion object {
        // Preference keys
        private val AUTO_LOCK_TIMEOUT = intPreferencesKey("auto_lock_timeout")
        private val CLIPBOARD_CLEAR_TIMEOUT = intPreferencesKey("clipboard_clear_timeout")
        private val FAILED_ATTEMPTS_LIMIT = intPreferencesKey("failed_attempts_limit")
        private val BIOMETRIC_ENABLED = booleanPreferencesKey("biometric_enabled")
        private val REQUIRE_PASSWORD_SENSITIVE = booleanPreferencesKey("require_password_sensitive")
        private val AUTO_BACKUP_ENABLED = booleanPreferencesKey("auto_backup_enabled")
        private val AUTO_BACKUP_INTERVAL = stringPreferencesKey("auto_backup_interval")
        private val BACKUP_LOCATION = stringPreferencesKey("backup_location")
        private val SHOW_FAVICONS = booleanPreferencesKey("show_favicons")
        private val COMPACT_VIEW = booleanPreferencesKey("compact_view")
        private val REMEMBER_DATABASE = booleanPreferencesKey("remember_database")
        private val LAST_DATABASE_URI = stringPreferencesKey("last_database_uri")
    }
    
    // UI State
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()
    
    // Events
    private val _events = MutableSharedFlow<SettingsEvent>()
    val events: SharedFlow<SettingsEvent> = _events.asSharedFlow()
    
    init {
        loadSettings()
        checkBiometricAvailability()
    }
    
    private fun loadSettings() {
        viewModelScope.launch {
            context.dataStore.data.collect { prefs ->
                _uiState.update {
                    it.copy(
                        autoLockTimeout = prefs[AUTO_LOCK_TIMEOUT] ?: 120,
                        clipboardClearTimeout = prefs[CLIPBOARD_CLEAR_TIMEOUT] ?: 30,
                        failedAttemptsLimit = prefs[FAILED_ATTEMPTS_LIMIT] ?: 5,
                        biometricEnabled = prefs[BIOMETRIC_ENABLED] ?: false,
                        requirePasswordSensitive = prefs[REQUIRE_PASSWORD_SENSITIVE] ?: true,
                        autoBackupEnabled = prefs[AUTO_BACKUP_ENABLED] ?: false,
                        autoBackupInterval = prefs[AUTO_BACKUP_INTERVAL] ?: "daily",
                        backupLocation = prefs[BACKUP_LOCATION],
                        showFavicons = prefs[SHOW_FAVICONS] ?: true,
                        compactView = prefs[COMPACT_VIEW] ?: false,
                        rememberDatabase = prefs[REMEMBER_DATABASE] ?: true,
                        lastDatabaseUri = prefs[LAST_DATABASE_URI]
                    )
                }
            }
        }
    }
    
    private fun checkBiometricAvailability() {
        val availability = biometricHelper.isBiometricAvailable()
        _uiState.update { 
            it.copy(
                biometricAvailable = availability == BiometricAvailability.Available,
                biometricType = biometricHelper.getBiometricType()
            )
        }
    }
    
    /**
     * Updates auto-lock timeout.
     */
    fun updateAutoLockTimeout(seconds: Int) {
        viewModelScope.launch {
            context.dataStore.edit { prefs ->
                prefs[AUTO_LOCK_TIMEOUT] = seconds
            }
        }
    }
    
    /**
     * Updates clipboard clear timeout.
     */
    fun updateClipboardClearTimeout(seconds: Int) {
        viewModelScope.launch {
            context.dataStore.edit { prefs ->
                prefs[CLIPBOARD_CLEAR_TIMEOUT] = seconds
            }
        }
    }
    
    /**
     * Updates failed attempts limit.
     */
    fun updateFailedAttemptsLimit(limit: Int) {
        viewModelScope.launch {
            context.dataStore.edit { prefs ->
                prefs[FAILED_ATTEMPTS_LIMIT] = limit
            }
        }
    }
    
    /**
     * Toggles biometric unlock.
     */
    fun toggleBiometric(enabled: Boolean) {
        if (enabled && !_uiState.value.biometricAvailable) {
            viewModelScope.launch {
                _events.emit(SettingsEvent.BiometricNotAvailable)
            }
            return
        }
        
        viewModelScope.launch {
            context.dataStore.edit { prefs ->
                prefs[BIOMETRIC_ENABLED] = enabled
            }
        }
    }
    
    /**
     * Toggles require password for sensitive actions.
     */
    fun toggleRequirePasswordSensitive(enabled: Boolean) {
        viewModelScope.launch {
            context.dataStore.edit { prefs ->
                prefs[REQUIRE_PASSWORD_SENSITIVE] = enabled
            }
        }
    }
    
    /**
     * Toggles auto backup.
     */
    fun toggleAutoBackup(enabled: Boolean) {
        viewModelScope.launch {
            context.dataStore.edit { prefs ->
                prefs[AUTO_BACKUP_ENABLED] = enabled
            }
            // TODO: Schedule/cancel backup work
        }
    }
    
    /**
     * Updates auto backup interval.
     */
    fun updateAutoBackupInterval(interval: String) {
        viewModelScope.launch {
            context.dataStore.edit { prefs ->
                prefs[AUTO_BACKUP_INTERVAL] = interval
            }
            // TODO: Reschedule backup work
        }
    }
    
    /**
     * Updates backup location.
     */
    fun updateBackupLocation(uri: Uri) {
        viewModelScope.launch {
            context.dataStore.edit { prefs ->
                prefs[BACKUP_LOCATION] = uri.toString()
            }
        }
    }
    
    /**
     * Toggles show favicons.
     */
    fun toggleShowFavicons(enabled: Boolean) {
        viewModelScope.launch {
            context.dataStore.edit { prefs ->
                prefs[SHOW_FAVICONS] = enabled
            }
        }
    }
    
    /**
     * Toggles compact view.
     */
    fun toggleCompactView(enabled: Boolean) {
        viewModelScope.launch {
            context.dataStore.edit { prefs ->
                prefs[COMPACT_VIEW] = enabled
            }
        }
    }
    
    /**
     * Toggles remember database.
     */
    fun toggleRememberDatabase(enabled: Boolean) {
        viewModelScope.launch {
            context.dataStore.edit { prefs ->
                prefs[REMEMBER_DATABASE] = enabled
                if (!enabled) {
                    prefs.remove(LAST_DATABASE_URI)
                }
            }
        }
    }
    
    /**
     * Saves last database URI.
     */
    fun saveLastDatabaseUri(uri: Uri) {
        if (!_uiState.value.rememberDatabase) return
        
        viewModelScope.launch {
            context.dataStore.edit { prefs ->
                prefs[LAST_DATABASE_URI] = uri.toString()
            }
        }
    }
    
    /**
     * Creates a manual backup.
     */
    fun createManualBackup(uri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            val result = repository.createBackup(uri)
            
            result.fold(
                onSuccess = {
                    _uiState.update { it.copy(isLoading = false) }
                    _events.emit(SettingsEvent.BackupCreated)
                },
                onFailure = { error ->
                    _uiState.update { it.copy(isLoading = false) }
                    _events.emit(SettingsEvent.Error(error.message ?: "Failed to create backup"))
                }
            )
        }
    }
    
    /**
     * Changes master password.
     */
    fun changeMasterPassword(newPassword: CharArray) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            val result = repository.changeMasterPassword(newPassword)
            
            result.fold(
                onSuccess = {
                    _uiState.update { it.copy(isLoading = false) }
                    _events.emit(SettingsEvent.PasswordChanged)
                },
                onFailure = { error ->
                    _uiState.update { it.copy(isLoading = false) }
                    _events.emit(SettingsEvent.Error(error.message ?: "Failed to change password"))
                }
            )
        }
    }
    
    /**
     * Gets database info.
     */
    fun getDatabaseInfo(): DatabaseInfo {
        val db = repository.database.value
        return DatabaseInfo(
            name = db?.metadata?.databaseName ?: "Unknown",
            entryCount = db?.entries?.size ?: 0,
            groupCount = db?.groups?.size ?: 0,
            createdAt = db?.metadata?.creationTime?.toString() ?: "Unknown",
            modifiedAt = db?.metadata?.lastModificationTime?.toString() ?: "Unknown"
        )
    }
}

/**
 * UI State for settings screen.
 */
data class SettingsUiState(
    val isLoading: Boolean = false,
    
    // Security settings
    val autoLockTimeout: Int = 120, // seconds
    val clipboardClearTimeout: Int = 30, // seconds
    val failedAttemptsLimit: Int = 5,
    val biometricEnabled: Boolean = false,
    val biometricAvailable: Boolean = false,
    val biometricType: String = "Biometric",
    val requirePasswordSensitive: Boolean = true,
    
    // Backup settings
    val autoBackupEnabled: Boolean = false,
    val autoBackupInterval: String = "daily",
    val backupLocation: String? = null,
    
    // Appearance settings
    val showFavicons: Boolean = true,
    val compactView: Boolean = false,
    
    // Database settings
    val rememberDatabase: Boolean = true,
    val lastDatabaseUri: String? = null
)

/**
 * Database info for display.
 */
data class DatabaseInfo(
    val name: String,
    val entryCount: Int,
    val groupCount: Int,
    val createdAt: String,
    val modifiedAt: String
)

/**
 * Events emitted by settings view model.
 */
sealed class SettingsEvent {
    object BackupCreated : SettingsEvent()
    object PasswordChanged : SettingsEvent()
    object BiometricNotAvailable : SettingsEvent()
    data class Error(val message: String) : SettingsEvent()
}
