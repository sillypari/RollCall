package com.pesky.app.viewmodels

import android.content.Context
import android.net.Uri
import androidx.datastore.preferences.core.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pesky.app.data.preferences.AppPreferences
import com.pesky.app.data.preferences.peskyDataStore
import com.pesky.app.data.repository.VaultRepository
import com.pesky.app.utils.BiometricAvailability
import com.pesky.app.utils.BiometricHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: VaultRepository,
    private val biometricHelper: BiometricHelper,
    private val appPreferences: AppPreferences
) : ViewModel() {
    
    companion object {
        // Preference keys
        private val AUTO_LOCK_TIMEOUT = intPreferencesKey("auto_lock_timeout")
        private val CLIPBOARD_CLEAR_TIMEOUT = intPreferencesKey("clipboard_clear_timeout")
        private val FAILED_ATTEMPTS_LIMIT = intPreferencesKey("failed_attempts_limit")
        private val BIOMETRIC_ENABLED = booleanPreferencesKey("biometric_enabled")
        private val REQUIRE_PASSWORD_SENSITIVE = booleanPreferencesKey("require_password_sensitive")
        val SCREENSHOT_PROTECTION_ENABLED = booleanPreferencesKey("screenshot_protection_enabled")
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
            context.peskyDataStore.data.collect { prefs ->
                _uiState.update {
                    it.copy(
                        autoLockTimeout = prefs[AUTO_LOCK_TIMEOUT] ?: 120,
                        clipboardClearTimeout = prefs[CLIPBOARD_CLEAR_TIMEOUT] ?: 30,
                        failedAttemptsLimit = prefs[FAILED_ATTEMPTS_LIMIT] ?: 5,
                        biometricEnabled = prefs[BIOMETRIC_ENABLED] ?: false,
                        requirePasswordSensitive = prefs[REQUIRE_PASSWORD_SENSITIVE] ?: true,
                        screenshotProtectionEnabled = prefs[SCREENSHOT_PROTECTION_ENABLED] ?: true,
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
            context.peskyDataStore.edit { prefs ->
                prefs[AUTO_LOCK_TIMEOUT] = seconds
            }
        }
    }
    
    /**
     * Updates clipboard clear timeout.
     */
    fun updateClipboardClearTimeout(seconds: Int) {
        viewModelScope.launch {
            context.peskyDataStore.edit { prefs ->
                prefs[CLIPBOARD_CLEAR_TIMEOUT] = seconds
            }
        }
    }
    
    /**
     * Updates failed attempts limit.
     */
    fun updateFailedAttemptsLimit(limit: Int) {
        viewModelScope.launch {
            context.peskyDataStore.edit { prefs ->
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
            context.peskyDataStore.edit { prefs ->
                prefs[BIOMETRIC_ENABLED] = enabled
            }
        }
    }
    
    /**
     * Toggles require password for sensitive actions.
     */
    fun toggleRequirePasswordSensitive(enabled: Boolean) {
        viewModelScope.launch {
            context.peskyDataStore.edit { prefs ->
                prefs[REQUIRE_PASSWORD_SENSITIVE] = enabled
            }
        }
    }
    
    /**
     * Toggles screenshot protection.
     */
    fun toggleScreenshotProtection(enabled: Boolean) {
        viewModelScope.launch {
            context.peskyDataStore.edit { prefs ->
                prefs[SCREENSHOT_PROTECTION_ENABLED] = enabled
            }
        }
    }
    
    /**
     * Toggles auto backup.
     */
    fun toggleAutoBackup(enabled: Boolean) {
        viewModelScope.launch {
            context.peskyDataStore.edit { prefs ->
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
            context.peskyDataStore.edit { prefs ->
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
            context.peskyDataStore.edit { prefs ->
                prefs[BACKUP_LOCATION] = uri.toString()
            }
        }
    }
    
    /**
     * Toggles show favicons.
     */
    fun toggleShowFavicons(enabled: Boolean) {
        viewModelScope.launch {
            context.peskyDataStore.edit { prefs ->
                prefs[SHOW_FAVICONS] = enabled
            }
        }
    }
    
    /**
     * Toggles compact view.
     */
    fun toggleCompactView(enabled: Boolean) {
        viewModelScope.launch {
            context.peskyDataStore.edit { prefs ->
                prefs[COMPACT_VIEW] = enabled
            }
        }
    }
    
    /**
     * Toggles remember database.
     */
    fun toggleRememberDatabase(enabled: Boolean) {
        viewModelScope.launch {
            context.peskyDataStore.edit { prefs ->
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
            context.peskyDataStore.edit { prefs ->
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
    
    /**
     * Check if quick unlock PIN is enabled.
     */
    fun isQuickUnlockEnabled(): Boolean = appPreferences.quickUnlockEnabled
    
    /**
     * Set up or change the quick unlock PIN.
     */
    fun setupQuickUnlockPin(pin: String) {
        appPreferences.setupQuickUnlock(pin, "pin")
        viewModelScope.launch {
            _events.emit(SettingsEvent.PinChanged)
        }
    }
    
    /**
     * Remove quick unlock PIN.
     */
    fun removeQuickUnlock() {
        appPreferences.removeQuickUnlock()
        viewModelScope.launch {
            _events.emit(SettingsEvent.PinRemoved)
        }
    }
    
    /**
     * Verify current PIN for changing.
     */
    fun verifyCurrentPin(pin: String): Boolean {
        return appPreferences.verifyQuickUnlock(pin)
    }
    
    /**
     * Clear all app data and reset (logout).
     */
    fun clearAllData() {
        viewModelScope.launch {
            // Clear preferences
            appPreferences.clearAll()
            
            // Clear internal database files
            val dbDir = File(context.filesDir, "databases")
            if (dbDir.exists()) {
                dbDir.deleteRecursively()
            }
            
            // Lock vault
            repository.lockVault()
            
            _events.emit(SettingsEvent.DataCleared)
        }
    }
    
    /**
     * Export database to external location.
     */
    fun exportDatabase(uri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            val result = repository.exportDatabase(uri)
            
            result.fold(
                onSuccess = {
                    _uiState.update { it.copy(isLoading = false) }
                    _events.emit(SettingsEvent.BackupCreated)
                },
                onFailure = { error ->
                    _uiState.update { it.copy(isLoading = false) }
                    _events.emit(SettingsEvent.Error(error.message ?: "Failed to export"))
                }
            )
        }
    }
    
    /**
     * Import database from external backup file.
     * This replaces the current database and requires the app to restart.
     */
    fun importDatabase(uri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            val result = repository.importDatabase(uri)
            
            result.fold(
                onSuccess = {
                    _uiState.update { it.copy(isLoading = false) }
                    _events.emit(SettingsEvent.BackupRestored)
                },
                onFailure = { error ->
                    _uiState.update { it.copy(isLoading = false) }
                    _events.emit(SettingsEvent.Error(error.message ?: "Failed to import backup"))
                }
            )
        }
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
    val screenshotProtectionEnabled: Boolean = true,
    
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
    object BackupRestored : SettingsEvent()
    object PasswordChanged : SettingsEvent()
    object BiometricNotAvailable : SettingsEvent()
    object PinChanged : SettingsEvent()
    object PinRemoved : SettingsEvent()
    object DataCleared : SettingsEvent()
    data class Error(val message: String) : SettingsEvent()
}
