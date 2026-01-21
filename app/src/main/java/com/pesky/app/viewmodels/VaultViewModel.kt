package com.pesky.app.viewmodels

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pesky.app.data.models.*
import com.pesky.app.data.preferences.AppPreferences
import com.pesky.app.data.repository.VaultRepository
import com.pesky.app.data.repository.VaultState
import com.pesky.app.ui.components.GroupItem
import com.pesky.app.ui.components.SidebarItem
import com.pesky.app.utils.PasswordStrengthAnalyzer
import com.pesky.app.utils.SecureClipboardManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VaultViewModel @Inject constructor(
    private val repository: VaultRepository,
    private val passwordStrengthAnalyzer: PasswordStrengthAnalyzer,
    private val clipboardManager: SecureClipboardManager,
    private val appPreferences: AppPreferences
) : ViewModel() {
    
    // UI State
    private val _uiState = MutableStateFlow(VaultUiState())
    val uiState: StateFlow<VaultUiState> = _uiState.asStateFlow()
    
    // Vault state from repository
    val vaultState: StateFlow<VaultState> = repository.vaultState
    
    // Current database
    val database: StateFlow<VaultDatabase?> = repository.database
    
    // Events
    private val _events = MutableSharedFlow<VaultEvent>()
    val events: SharedFlow<VaultEvent> = _events.asSharedFlow()
    
    init {
        // Observe database changes
        viewModelScope.launch {
            repository.database.collect { db ->
                if (db != null) {
                    updateEntriesList()
                }
            }
        }
    }
    
    /**
     * Opens a database file.
     */
    fun openDatabase(uri: Uri, masterPassword: CharArray) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            val result = repository.openDatabase(uri, masterPassword)
            
            result.fold(
                onSuccess = {
                    _uiState.update { it.copy(isLoading = false) }
                    updateEntriesList()
                    _events.emit(VaultEvent.DatabaseOpened)
                },
                onFailure = { error ->
                    _uiState.update { 
                        it.copy(
                            isLoading = false, 
                            error = error.message,
                            remainingAttempts = repository.getRemainingAttempts()
                        )
                    }
                    _events.emit(VaultEvent.Error(error.message ?: "Failed to open database"))
                }
            )
        }
    }
    
    /**
     * Creates a new database.
     */
    fun createDatabase(uri: Uri, masterPassword: CharArray, name: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            val result = repository.createDatabase(uri, masterPassword, name)
            
            result.fold(
                onSuccess = {
                    // Save to preferences that database was created
                    appPreferences.onDatabaseCreated(name)
                    _uiState.update { it.copy(isLoading = false) }
                    _events.emit(VaultEvent.DatabaseCreated)
                },
                onFailure = { error ->
                    _uiState.update { it.copy(isLoading = false, error = error.message) }
                    _events.emit(VaultEvent.Error(error.message ?: "Failed to create database"))
                }
            )
        }
    }
    
    /**
     * Locks the vault.
     */
    fun lockVault() {
        repository.lockVault()
        _uiState.update { VaultUiState() }
        viewModelScope.launch {
            _events.emit(VaultEvent.VaultLocked)
        }
    }
    
    /**
     * Updates the search query.
     */
    fun updateSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        updateEntriesList()
    }
    
    /**
     * Copies a password to clipboard.
     */
    fun copyPassword(entry: PasswordEntry) {
        clipboardManager.copyWithTimeout(entry.password, "Password", isSensitive = true)
        viewModelScope.launch {
            repository.recordEntryAccess(entry.uuid)
            _events.emit(VaultEvent.PasswordCopied)
        }
    }
    
    /**
     * Copies username to clipboard.
     */
    fun copyUsername(entry: PasswordEntry) {
        clipboardManager.copyWithTimeout(entry.userName, "Username", isSensitive = false)
        viewModelScope.launch {
            _events.emit(VaultEvent.UsernameCopied)
        }
    }
    
    /**
     * Toggles favorite status.
     */
    fun toggleFavorite(entryUuid: String) {
        viewModelScope.launch {
            repository.toggleFavorite(entryUuid)
            updateEntriesList()
        }
    }
    
    /**
     * Deletes an entry.
     */
    fun deleteEntry(entryUuid: String) {
        viewModelScope.launch {
            val result = repository.deleteEntry(entryUuid)
            result.fold(
                onSuccess = {
                    updateEntriesList()
                    _events.emit(VaultEvent.EntryDeleted)
                },
                onFailure = { error ->
                    _events.emit(VaultEvent.Error(error.message ?: "Failed to delete entry"))
                }
            )
        }
    }
    
    /**
     * Gets password strength for an entry.
     */
    fun getPasswordStrength(password: String): PasswordStrength {
        return passwordStrengthAnalyzer.analyze(password).strength
    }
    
    /**
     * Gets the groups for sidebar.
     */
    fun getGroups(): List<GroupItem> {
        val db = repository.database.value ?: return emptyList()
        return db.groups.filter { it.uuid != "root" }.map { group ->
            GroupItem(
                uuid = group.uuid,
                name = group.name,
                count = db.entries.count { it.groupUuid == group.uuid }
            )
        }
    }
    
    /**
     * Creates a new group.
     */
    fun createGroup(name: String) {
        viewModelScope.launch {
            val group = Group(
                uuid = java.util.UUID.randomUUID().toString(),
                name = name
            )
            val result = repository.addGroup(group)
            result.fold(
                onSuccess = {
                    updateEntriesList()
                    _events.emit(VaultEvent.GroupCreated(name))
                },
                onFailure = { error ->
                    _events.emit(VaultEvent.Error(error.message ?: "Failed to create group"))
                }
            )
        }
    }
    
    /**
     * Sets the selected group for filtering.
     */
    fun selectGroup(groupUuid: String?) {
        _uiState.update { it.copy(selectedGroupUuid = groupUuid) }
        updateEntriesList()
    }
    
    private fun updateEntriesList() {
        val query = _uiState.value.searchQuery.trim().lowercase()
        val selectedGroupUuid = _uiState.value.selectedGroupUuid
        
        // Smart search: check for special keywords first
        var entries = when {
            query.isEmpty() -> repository.getAllEntries()
            query in listOf("weak", "weak password", "weak passwords") -> repository.getWeakPasswordEntries()
            query in listOf("duplicate", "duplicates", "duplicate password", "duplicate passwords", "reused") -> repository.getDuplicatePasswordEntries()
            query in listOf("expiring", "expiring soon", "expires", "old") -> repository.getExpiringSoonEntries()
            query in listOf("note", "notes", "secure note", "secure notes") -> repository.getSecureNotes()
            query in listOf("favorite", "favorites", "starred", "star") -> repository.getFavorites()
            query in listOf("recent", "recently used", "recent passwords") -> repository.getRecentlyUsed()
            else -> repository.searchEntries(query)
        }
        
        // Filter by selected group if one is selected (for Groups tab)
        if (selectedGroupUuid != null) {
            entries = entries.filter { it.groupUuid == selectedGroupUuid }
        }
        
        _uiState.update { 
            it.copy(
                entries = entries,
                groups = getGroups()
            )
        }
    }
}

/**
 * UI State for the vault screen.
 */
data class VaultUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val remainingAttempts: Int = 5,
    val searchQuery: String = "",
    val entries: List<PasswordEntry> = emptyList(),
    val selectedGroupUuid: String? = null,
    val groups: List<GroupItem> = emptyList()
)

/**
 * Events emitted by the vault view model.
 */
sealed class VaultEvent {
    object DatabaseOpened : VaultEvent()
    object DatabaseCreated : VaultEvent()
    object VaultLocked : VaultEvent()
    object PasswordCopied : VaultEvent()
    object UsernameCopied : VaultEvent()
    object EntryDeleted : VaultEvent()
    data class GroupCreated(val name: String) : VaultEvent()
    data class Error(val message: String) : VaultEvent()
}
