package com.pesky.app.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pesky.app.data.models.*
import com.pesky.app.data.repository.VaultRepository
import com.pesky.app.utils.PasswordGenerator
import com.pesky.app.utils.PasswordGeneratorOptions
import com.pesky.app.utils.PasswordStrengthAnalyzer
import com.pesky.app.utils.PasswordAnalysisResult
import com.pesky.app.utils.SecureClipboardManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.Instant
import javax.inject.Inject

@HiltViewModel
class EntryViewModel @Inject constructor(
    private val repository: VaultRepository,
    private val passwordGenerator: PasswordGenerator,
    private val passwordStrengthAnalyzer: PasswordStrengthAnalyzer,
    private val clipboardManager: SecureClipboardManager,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    
    private val entryUuid: String? = savedStateHandle["entryUuid"]
    
    // UI State
    private val _uiState = MutableStateFlow(EntryUiState())
    val uiState: StateFlow<EntryUiState> = _uiState.asStateFlow()
    
    // Events
    private val _events = MutableSharedFlow<EntryEvent>()
    val events: SharedFlow<EntryEvent> = _events.asSharedFlow()
    
    init {
        if (entryUuid != null) {
            loadEntry(entryUuid)
        }
    }
    
    /**
     * Loads an existing entry for editing.
     */
    fun loadEntry(uuid: String) {
        val entry = repository.database.value?.entries?.find { it.uuid == uuid }
        if (entry != null) {
            _uiState.update { 
                it.copy(
                    entry = entry,
                    title = entry.title,
                    userName = entry.userName,
                    password = entry.password,
                    url = entry.url,
                    notes = entry.notes,
                    tags = entry.tags,
                    customFields = entry.customFields,
                    selectedGroupUuid = entry.groupUuid,
                    isFavorite = entry.isFavorite,
                    passwordAnalysis = passwordStrengthAnalyzer.analyze(entry.password),
                    isEditing = true
                )
            }
        }
    }
    
    /**
     * Updates the title field.
     */
    fun updateTitle(title: String) {
        _uiState.update { 
            it.copy(
                title = title,
                titleError = if (title.isBlank()) "Title is required" else null
            )
        }
    }
    
    /**
     * Updates the username field.
     */
    fun updateUserName(userName: String) {
        _uiState.update { it.copy(userName = userName) }
    }
    
    /**
     * Updates the password field.
     */
    fun updatePassword(password: String) {
        _uiState.update { 
            it.copy(
                password = password,
                passwordAnalysis = passwordStrengthAnalyzer.analyze(password)
            )
        }
    }
    
    /**
     * Updates the URL field.
     */
    fun updateUrl(url: String) {
        _uiState.update { it.copy(url = url) }
    }
    
    /**
     * Updates the notes field.
     */
    fun updateNotes(notes: String) {
        _uiState.update { it.copy(notes = notes) }
    }
    
    /**
     * Updates tags.
     */
    fun updateTags(tags: List<String>) {
        _uiState.update { it.copy(tags = tags) }
    }
    
    /**
     * Adds a tag.
     */
    fun addTag(tag: String) {
        if (tag.isNotBlank() && tag !in _uiState.value.tags) {
            _uiState.update { it.copy(tags = it.tags + tag.trim()) }
        }
    }
    
    /**
     * Removes a tag.
     */
    fun removeTag(tag: String) {
        _uiState.update { it.copy(tags = it.tags.filter { it != tag }) }
    }
    
    /**
     * Updates selected group.
     */
    fun updateSelectedGroup(groupUuid: String?) {
        _uiState.update { it.copy(selectedGroupUuid = groupUuid) }
    }
    
    /**
     * Toggles favorite.
     */
    fun toggleFavorite() {
        _uiState.update { it.copy(isFavorite = !it.isFavorite) }
    }
    
    /**
     * Adds a custom field.
     */
    fun addCustomField(key: String, value: String, isProtected: Boolean) {
        val newField = CustomField(key, value, isProtected)
        _uiState.update { it.copy(customFields = it.customFields + newField) }
    }
    
    /**
     * Removes a custom field.
     */
    fun removeCustomField(index: Int) {
        _uiState.update { 
            it.copy(customFields = it.customFields.filterIndexed { i, _ -> i != index })
        }
    }
    
    /**
     * Updates a custom field.
     */
    fun updateCustomField(index: Int, field: CustomField) {
        _uiState.update {
            it.copy(customFields = it.customFields.mapIndexed { i, f -> 
                if (i == index) field else f 
            })
        }
    }
    
    /**
     * Generates a new password.
     */
    fun generatePassword(options: PasswordGeneratorOptions = PasswordGeneratorOptions()) {
        val newPassword = passwordGenerator.generate(options)
        updatePassword(newPassword)
        _uiState.update { it.copy(generatorOptions = options) }
    }
    
    /**
     * Updates password generator options.
     */
    fun updateGeneratorOptions(options: PasswordGeneratorOptions) {
        _uiState.update { it.copy(generatorOptions = options) }
    }
    
    /**
     * Toggles password visibility.
     */
    fun togglePasswordVisibility() {
        _uiState.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
    }
    
    /**
     * Toggles password generator modal.
     */
    fun togglePasswordGenerator() {
        _uiState.update { it.copy(showPasswordGenerator = !it.showPasswordGenerator) }
    }
    
    /**
     * Copies password to clipboard.
     */
    fun copyPassword() {
        clipboardManager.copyWithTimeout(_uiState.value.password, "Password", isSensitive = true)
        viewModelScope.launch {
            _events.emit(EntryEvent.PasswordCopied)
        }
    }
    
    /**
     * Copies username to clipboard.
     */
    fun copyUsername() {
        clipboardManager.copyWithTimeout(_uiState.value.userName, "Username", isSensitive = false)
        viewModelScope.launch {
            _events.emit(EntryEvent.UsernameCopied)
        }
    }
    
    /**
     * Validates and saves the entry.
     */
    fun saveEntry() {
        val state = _uiState.value
        
        // Validate
        if (state.title.isBlank()) {
            _uiState.update { it.copy(titleError = "Title is required") }
            return
        }
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            val entry = if (state.isEditing && state.entry != null) {
                // Update existing entry
                val oldPassword = state.entry.password
                val newPassword = state.password
                
                if (oldPassword != newPassword) {
                    state.entry.withUpdatedPassword(newPassword).copy(
                        title = state.title,
                        userName = state.userName,
                        url = state.url,
                        notes = state.notes,
                        tags = state.tags,
                        customFields = state.customFields,
                        groupUuid = state.selectedGroupUuid,
                        isFavorite = state.isFavorite
                    )
                } else {
                    state.entry.copy(
                        title = state.title,
                        userName = state.userName,
                        password = state.password,
                        url = state.url,
                        notes = state.notes,
                        tags = state.tags,
                        customFields = state.customFields,
                        groupUuid = state.selectedGroupUuid,
                        isFavorite = state.isFavorite,
                        times = state.entry.times.copy(lastModificationTime = Instant.now())
                    )
                }
            } else {
                // Create new entry
                PasswordEntry(
                    title = state.title,
                    userName = state.userName,
                    password = state.password,
                    url = state.url,
                    notes = state.notes,
                    tags = state.tags,
                    customFields = state.customFields,
                    groupUuid = state.selectedGroupUuid,
                    isFavorite = state.isFavorite
                )
            }
            
            val result = if (state.isEditing) {
                repository.updateEntry(entry)
            } else {
                repository.addEntry(entry)
            }
            
            result.fold(
                onSuccess = {
                    _uiState.update { it.copy(isLoading = false) }
                    _events.emit(EntryEvent.EntrySaved)
                },
                onFailure = { error ->
                    _uiState.update { it.copy(isLoading = false) }
                    _events.emit(EntryEvent.Error(error.message ?: "Failed to save entry"))
                }
            )
        }
    }
    
    /**
     * Deletes the current entry.
     */
    fun deleteEntry() {
        val entry = _uiState.value.entry ?: return
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            val result = repository.deleteEntry(entry.uuid)
            
            result.fold(
                onSuccess = {
                    _uiState.update { it.copy(isLoading = false) }
                    _events.emit(EntryEvent.EntryDeleted)
                },
                onFailure = { error ->
                    _uiState.update { it.copy(isLoading = false) }
                    _events.emit(EntryEvent.Error(error.message ?: "Failed to delete entry"))
                }
            )
        }
    }
    
    /**
     * Gets available groups for selection.
     */
    fun getAvailableGroups(): List<Pair<String?, String>> {
        val groups = repository.getGroups()
        return listOf(null to "No Category") + groups.map { it.uuid to it.name }
    }
}

/**
 * UI State for entry screen.
 */
data class EntryUiState(
    val isLoading: Boolean = false,
    val isEditing: Boolean = false,
    val entry: PasswordEntry? = null,
    
    // Form fields
    val title: String = "",
    val titleError: String? = null,
    val userName: String = "",
    val password: String = "",
    val url: String = "",
    val notes: String = "",
    val tags: List<String> = emptyList(),
    val customFields: List<CustomField> = emptyList(),
    val selectedGroupUuid: String? = null,
    val isFavorite: Boolean = false,
    
    // UI state
    val isPasswordVisible: Boolean = false,
    val showPasswordGenerator: Boolean = false,
    val generatorOptions: PasswordGeneratorOptions = PasswordGeneratorOptions(),
    val passwordAnalysis: PasswordAnalysisResult = PasswordAnalysisResult(
        strength = PasswordStrength.VERY_WEAK,
        score = 0,
        entropy = 0.0,
        feedback = emptyList()
    )
)

/**
 * Events emitted by the entry view model.
 */
sealed class EntryEvent {
    object EntrySaved : EntryEvent()
    object EntryDeleted : EntryEvent()
    object PasswordCopied : EntryEvent()
    object UsernameCopied : EntryEvent()
    data class Error(val message: String) : EntryEvent()
}
