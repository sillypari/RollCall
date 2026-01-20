package com.pesky.app.data.repository

import android.content.Context
import android.net.Uri
import com.pesky.app.data.database.PeskyDatabaseHandler
import com.pesky.app.data.database.WrongPasswordException
import com.pesky.app.data.models.*
import com.pesky.app.utils.PasswordStrengthAnalyzer
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import java.io.FileOutputStream
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for vault operations.
 * Provides a clean API for UI layer to interact with the database.
 */
@Singleton
class VaultRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val databaseHandler: PeskyDatabaseHandler,
    private val passwordStrengthAnalyzer: PasswordStrengthAnalyzer
) {
    
    private val _vaultState = MutableStateFlow<VaultState>(VaultState.Locked)
    val vaultState: StateFlow<VaultState> = _vaultState.asStateFlow()
    
    private val _database = MutableStateFlow<VaultDatabase?>(null)
    val database: StateFlow<VaultDatabase?> = _database.asStateFlow()
    
    private val _currentFilePath = MutableStateFlow<Uri?>(null)
    val currentFilePath: StateFlow<Uri?> = _currentFilePath.asStateFlow()
    
    private var failedAttempts = 0
    private val maxFailedAttempts = 5
    
    /**
     * Creates a new database at the specified location.
     */
    suspend fun createDatabase(
        uri: Uri,
        masterPassword: CharArray,
        databaseName: String
    ): Result<Unit> {
        return try {
            val outputStream = context.contentResolver.openOutputStream(uri)
                ?: return Result.failure(Exception("Cannot open file for writing"))
            
            val result = databaseHandler.createDatabase(outputStream, masterPassword, databaseName)
            
            result.fold(
                onSuccess = { db ->
                    _database.value = db
                    _currentFilePath.value = uri
                    _vaultState.value = VaultState.Unlocked
                    failedAttempts = 0
                    Result.success(Unit)
                },
                onFailure = { Result.failure(it) }
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Opens an existing database.
     */
    suspend fun openDatabase(uri: Uri, masterPassword: CharArray): Result<Unit> {
        if (failedAttempts >= maxFailedAttempts) {
            return Result.failure(Exception("Too many failed attempts. Please wait."))
        }
        
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
                ?: return Result.failure(Exception("Cannot open file for reading"))
            
            val result = databaseHandler.openDatabase(inputStream, masterPassword, uri.toString())
            
            result.fold(
                onSuccess = { db ->
                    _database.value = db
                    _currentFilePath.value = uri
                    _vaultState.value = VaultState.Unlocked
                    failedAttempts = 0
                    Result.success(Unit)
                },
                onFailure = { error ->
                    if (error is WrongPasswordException) {
                        failedAttempts++
                    }
                    Result.failure(error)
                }
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Locks the vault.
     */
    fun lockVault() {
        databaseHandler.lockVault()
        _database.value = null
        _vaultState.value = VaultState.Locked
    }
    
    /**
     * Adds a new password entry.
     */
    suspend fun addEntry(entry: PasswordEntry): Result<Unit> {
        val currentDb = _database.value ?: return Result.failure(Exception("No database loaded"))
        val uri = _currentFilePath.value ?: return Result.failure(Exception("No file path"))
        
        val updatedDb = currentDb.addEntry(entry)
        
        return saveDatabase(uri, updatedDb)
    }
    
    /**
     * Updates an existing entry.
     */
    suspend fun updateEntry(entry: PasswordEntry): Result<Unit> {
        val currentDb = _database.value ?: return Result.failure(Exception("No database loaded"))
        val uri = _currentFilePath.value ?: return Result.failure(Exception("No file path"))
        
        val updatedEntry = entry.copy(
            times = entry.times.copy(lastModificationTime = Instant.now())
        )
        val updatedDb = currentDb.updateEntry(updatedEntry)
        
        return saveDatabase(uri, updatedDb)
    }
    
    /**
     * Deletes an entry.
     */
    suspend fun deleteEntry(entryUuid: String): Result<Unit> {
        val currentDb = _database.value ?: return Result.failure(Exception("No database loaded"))
        val uri = _currentFilePath.value ?: return Result.failure(Exception("No file path"))
        
        val updatedDb = currentDb.deleteEntry(entryUuid)
        
        return saveDatabase(uri, updatedDb)
    }
    
    /**
     * Records access time for an entry.
     */
    suspend fun recordEntryAccess(entryUuid: String): Result<Unit> {
        val currentDb = _database.value ?: return Result.failure(Exception("No database loaded"))
        
        val entry = currentDb.entries.find { it.uuid == entryUuid } ?: return Result.success(Unit)
        val updatedEntry = entry.recordAccess()
        val updatedDb = currentDb.updateEntry(updatedEntry)
        
        databaseHandler.updateCachedDatabase(updatedDb)
        _database.value = updatedDb
        
        return Result.success(Unit)
    }
    
    /**
     * Toggles favorite status for an entry.
     */
    suspend fun toggleFavorite(entryUuid: String): Result<Unit> {
        val currentDb = _database.value ?: return Result.failure(Exception("No database loaded"))
        val uri = _currentFilePath.value ?: return Result.failure(Exception("No file path"))
        
        val entry = currentDb.entries.find { it.uuid == entryUuid } 
            ?: return Result.failure(Exception("Entry not found"))
        
        val updatedEntry = entry.copy(isFavorite = !entry.isFavorite)
        val updatedDb = currentDb.updateEntry(updatedEntry)
        
        return saveDatabase(uri, updatedDb)
    }
    
    /**
     * Adds a new group (category).
     */
    suspend fun addGroup(group: Group): Result<Unit> {
        val currentDb = _database.value ?: return Result.failure(Exception("No database loaded"))
        val uri = _currentFilePath.value ?: return Result.failure(Exception("No file path"))
        
        val updatedDb = currentDb.addGroup(group)
        
        return saveDatabase(uri, updatedDb)
    }
    
    /**
     * Searches entries by query.
     */
    fun searchEntries(query: String): List<PasswordEntry> {
        return _database.value?.search(query) ?: emptyList()
    }
    
    /**
     * Gets all entries.
     */
    fun getAllEntries(): List<PasswordEntry> {
        return _database.value?.entries ?: emptyList()
    }
    
    /**
     * Gets favorite entries.
     */
    fun getFavorites(): List<PasswordEntry> {
        return _database.value?.getFavorites() ?: emptyList()
    }
    
    /**
     * Gets recently used entries.
     */
    fun getRecentlyUsed(limit: Int = 10): List<PasswordEntry> {
        return _database.value?.getRecentlyUsed(limit) ?: emptyList()
    }
    
    /**
     * Gets entries with weak passwords.
     */
    fun getWeakPasswordEntries(): List<PasswordEntry> {
        return _database.value?.getWeakPasswordEntries { password ->
            passwordStrengthAnalyzer.analyze(password).strength
        } ?: emptyList()
    }
    
    /**
     * Gets entries with duplicate passwords.
     */
    fun getDuplicatePasswordEntries(): List<PasswordEntry> {
        return _database.value?.getDuplicatePasswordEntries() ?: emptyList()
    }
    
    /**
     * Gets entries expiring soon.
     */
    fun getExpiringSoonEntries(): List<PasswordEntry> {
        return _database.value?.getExpiringSoonEntries() ?: emptyList()
    }
    
    /**
     * Gets secure notes.
     */
    fun getSecureNotes(): List<PasswordEntry> {
        return _database.value?.getSecureNotes() ?: emptyList()
    }
    
    /**
     * Gets entries for a specific group.
     */
    fun getEntriesForGroup(groupUuid: String?): List<PasswordEntry> {
        return _database.value?.getEntriesForGroup(groupUuid) ?: emptyList()
    }
    
    /**
     * Gets all groups.
     */
    fun getGroups(): List<Group> {
        return _database.value?.groups ?: emptyList()
    }
    
    /**
     * Gets category counts for sidebar.
     */
    fun getCategoryCounts(): Map<String, Int> {
        return _database.value?.getCategoryCounts() ?: emptyMap()
    }
    
    /**
     * Gets the number of remaining attempts.
     */
    fun getRemainingAttempts(): Int {
        return maxFailedAttempts - failedAttempts
    }
    
    /**
     * Creates a backup of the current database.
     */
    suspend fun createBackup(backupUri: Uri): Result<Unit> {
        val sourceUri = _currentFilePath.value ?: return Result.failure(Exception("No database loaded"))
        
        return try {
            val inputStream = context.contentResolver.openInputStream(sourceUri)
                ?: return Result.failure(Exception("Cannot read source file"))
            
            val outputStream = context.contentResolver.openOutputStream(backupUri)
                ?: return Result.failure(Exception("Cannot write backup file"))
            
            inputStream.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Changes the master password.
     */
    suspend fun changeMasterPassword(newPassword: CharArray): Result<Unit> {
        val uri = _currentFilePath.value ?: return Result.failure(Exception("No file path"))
        
        return try {
            val outputStream = context.contentResolver.openOutputStream(uri, "wt")
                ?: return Result.failure(Exception("Cannot open file for writing"))
            
            databaseHandler.changeMasterPassword(outputStream, newPassword)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private suspend fun saveDatabase(uri: Uri, database: VaultDatabase): Result<Unit> {
        return try {
            val outputStream = context.contentResolver.openOutputStream(uri, "wt")
                ?: return Result.failure(Exception("Cannot open file for writing"))
            
            val result = databaseHandler.saveDatabase(outputStream, database)
            
            result.fold(
                onSuccess = {
                    _database.value = database
                    Result.success(Unit)
                },
                onFailure = { Result.failure(it) }
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

/**
 * Represents the current state of the vault.
 */
sealed class VaultState {
    object Locked : VaultState()
    object Unlocked : VaultState()
    data class Error(val message: String) : VaultState()
}
