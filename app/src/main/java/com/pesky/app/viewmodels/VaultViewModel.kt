package com.pesky.app.viewmodels

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pesky.app.data.models.*
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
    private val clipboardManager: SecureClipboardManager
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
     * Selects a sidebar item.
     */
    fun selectSidebarItem(item: SidebarItem) {
        _uiState.update { it.copy(selectedSidebarItem = item) }
        updateEntriesList()
    }
    
    /**
     * Toggles the sidebar drawer.
     */
    fun toggleDrawer() {
        _uiState.update { it.copy(isDrawerOpen = !it.isDrawerOpen) }
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
     * Gets sidebar item counts.
     */
    fun getSidebarCounts(): Map<SidebarItem, Int> {
        val db = repository.database.value ?: return emptyMap()
        
        return mapOf(
            SidebarItem.AllItems to db.entries.size,
            SidebarItem.Favorites to repository.getFavorites().size,
            SidebarItem.RecentlyUsed to repository.getRecentlyUsed().size,
            SidebarItem.WeakPasswords to repository.getWeakPasswordEntries().size,
            SidebarItem.DuplicatePasswords to repository.getDuplicatePasswordEntries().size,
            SidebarItem.ExpiringSoon to repository.getExpiringSoonEntries().size,
            SidebarItem.SecureNotes to repository.getSecureNotes().size,
            SidebarItem.Trash to db.deletedObjects.size
        )
    }
    
    private fun updateEntriesList() {
        val query = _uiState.value.searchQuery
        val sidebarItem = _uiState.value.selectedSidebarItem
        
        val entries = when {
            query.isNotEmpty() -> repository.searchEntries(query)
            else -> when (sidebarItem) {
                SidebarItem.AllItems -> repository.getAllEntries()
                SidebarItem.Favorites -> repository.getFavorites()
                SidebarItem.RecentlyUsed -> repository.getRecentlyUsed()
                SidebarItem.WeakPasswords -> repository.getWeakPasswordEntries()
                SidebarItem.DuplicatePasswords -> repository.getDuplicatePasswordEntries()
                SidebarItem.ExpiringSoon -> repository.getExpiringSoonEntries()
                SidebarItem.SecureNotes -> repository.getSecureNotes()
                SidebarItem.Trash -> emptyList() // TODO: Implement trash
                is SidebarItem.Category -> repository.getEntriesForGroup(sidebarItem.uuid)
            }
        }
        
        _uiState.update { 
            it.copy(
                entries = entries,
                sidebarCounts = getSidebarCounts(),
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
    val selectedSidebarItem: SidebarItem = SidebarItem.AllItems,
    val isDrawerOpen: Boolean = false,
    val sidebarCounts: Map<SidebarItem, Int> = emptyMap(),
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
    data class Error(val message: String) : VaultEvent()
}
