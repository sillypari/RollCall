package com.pesky.app.data.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import com.pesky.app.data.database.PeskyDatabaseHandler
import com.pesky.app.data.database.WrongPasswordException
import com.pesky.app.data.models.*
import com.pesky.app.utils.PasswordStrengthAnalyzer
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "VaultRepository"

/**
 * Repository for vault operations.
 * Uses internal app storage for reliable database operations.
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
    
    // Internal storage file for the database
    private var internalDbFile: File? = null
    
    private var failedAttempts = 0
    private val maxFailedAttempts = 5
    
    /**
     * Gets the default internal database file.
     */
    private fun getInternalDbFile(name: String = "default"): File {
        val dbDir = File(context.filesDir, "databases")
        if (!dbDir.exists()) {
            dbDir.mkdirs()
        }
        return File(dbDir, "${name}.pesky")
    }
    
    /**
     * Creates a new database at the specified location.
     * Also saves to internal storage for reliability.
     */
    suspend fun createDatabase(
        uri: Uri,
        masterPassword: CharArray,
        databaseName: String
    ): Result<Unit> {
        return try {
            // Always use "current.pesky" for consistency between create/open/save
            val dbFile = getInternalDbFile("current")
            val outputStream = FileOutputStream(dbFile)
            
            Log.d(TAG, "Creating database at: ${dbFile.absolutePath}")
            
            val result = databaseHandler.createDatabase(outputStream, masterPassword, databaseName)
            
            result.fold(
                onSuccess = { db ->
                    Log.d(TAG, "Database created successfully, file size: ${dbFile.length()}")
                    internalDbFile = dbFile
                    _database.value = db
                    _currentFilePath.value = Uri.fromFile(dbFile)
                    _vaultState.value = VaultState.Unlocked
                    failedAttempts = 0
                    
                    // Also try to copy to the user-selected external URI (optional backup)
                    try {
                        context.contentResolver.openOutputStream(uri, "wt")?.use { extStream ->
                            dbFile.inputStream().use { it.copyTo(extStream) }
                        }
                        Log.d(TAG, "Also saved to external URI")
                    } catch (e: Exception) {
                        Log.w(TAG, "Could not save to external URI: ${e.message}")
                    }
                    
                    Result.success(Unit)
                },
                onFailure = { 
                    Log.e(TAG, "Failed to create database: ${it.message}")
                    Result.failure(it) 
                }
            )
        } catch (e: Exception) {
            Log.e(TAG, "Exception creating database: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    /**
     * Opens an existing database.
     * First checks internal storage, then falls back to external URI.
     */
    suspend fun openDatabase(uri: Uri, masterPassword: CharArray): Result<Unit> {
        if (failedAttempts >= maxFailedAttempts) {
            return Result.failure(Exception("Too many failed attempts. Please wait."))
        }
        
        return try {
            Log.d(TAG, "Opening database from: $uri")
            
            // First, check if we have an internal database file that we should use
            val internalFiles = File(context.filesDir, "databases").listFiles()
            val existingInternalDb = internalFiles?.firstOrNull { it.extension == "pesky" && it.length() > 0 }
            
            val fileToOpen: File
            if (existingInternalDb != null && existingInternalDb.length() > 0) {
                Log.d(TAG, "Found existing internal database: ${existingInternalDb.absolutePath}, size: ${existingInternalDb.length()}")
                fileToOpen = existingInternalDb
            } else {
                // Copy from external URI to internal storage
                val tempFile = File(context.cacheDir, "temp_db.pesky")
                context.contentResolver.openInputStream(uri)?.use { input ->
                    FileOutputStream(tempFile).use { output ->
                        input.copyTo(output)
                    }
                } ?: return Result.failure(Exception("Cannot open file for reading"))
                
                Log.d(TAG, "Copied from external to temp file, size: ${tempFile.length()}")
                
                if (tempFile.length() == 0L) {
                    tempFile.delete()
                    return Result.failure(Exception("Database file is empty (0 bytes). Try creating a new database."))
                }
                fileToOpen = tempFile
            }
            
            val inputStream = FileInputStream(fileToOpen)
            val result = databaseHandler.openDatabase(inputStream, masterPassword, uri.toString())
            
            result.fold(
                onSuccess = { db ->
                    Log.d(TAG, "Database opened successfully with ${db.entries.size} entries")
                    
                    // If we used a temp file, move it to permanent storage
                    if (fileToOpen.parent == context.cacheDir.absolutePath) {
                        val dbFile = getInternalDbFile("current")
                        fileToOpen.copyTo(dbFile, overwrite = true)
                        fileToOpen.delete()
                        internalDbFile = dbFile
                    } else {
                        internalDbFile = fileToOpen
                    }
                    
                    _database.value = db
                    _currentFilePath.value = uri
                    _vaultState.value = VaultState.Unlocked
                    failedAttempts = 0
                    Result.success(Unit)
                },
                onFailure = { error ->
                    Log.e(TAG, "Failed to open database: ${error.message}")
                    if (fileToOpen.parent == context.cacheDir.absolutePath) {
                        fileToOpen.delete()
                    }
                    if (error is WrongPasswordException) {
                        failedAttempts++
                    }
                    Result.failure(error)
                }
            )
        } catch (e: Exception) {
            Log.e(TAG, "Exception opening database: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    /**
     * Opens the database directly from internal storage.
     * Used for quick unlock when returning to the app.
     */
    suspend fun openDatabaseFromInternal(masterPassword: CharArray): Result<Unit> {
        return try {
            Log.d(TAG, "Opening database from internal storage")
            
            // Find the internal database file
            val dbDir = File(context.filesDir, "databases")
            val dbFile = dbDir.listFiles()?.firstOrNull { it.extension == "pesky" && it.length() > 0 }
                ?: return Result.failure(Exception("No database found. Please create or import a database."))
            
            Log.d(TAG, "Found database: ${dbFile.absolutePath}, size: ${dbFile.length()}")
            
            val inputStream = FileInputStream(dbFile)
            val result = databaseHandler.openDatabase(inputStream, masterPassword, dbFile.absolutePath)
            
            result.fold(
                onSuccess = { db ->
                    Log.d(TAG, "Database opened from internal storage with ${db.entries.size} entries")
                    internalDbFile = dbFile
                    _database.value = db
                    _currentFilePath.value = Uri.fromFile(dbFile)
                    _vaultState.value = VaultState.Unlocked
                    failedAttempts = 0
                    Result.success(Unit)
                },
                onFailure = { error ->
                    Log.e(TAG, "Failed to open database from internal: ${error.message}")
                    if (error is WrongPasswordException) {
                        failedAttempts++
                    }
                    Result.failure(error)
                }
            )
        } catch (e: Exception) {
            Log.e(TAG, "Exception opening database from internal: ${e.message}", e)
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
        val uri = _currentFilePath.value ?: return Result.failure(Exception("No file path"))
        
        val entry = currentDb.entries.find { it.uuid == entryUuid } ?: return Result.success(Unit)
        val updatedEntry = entry.recordAccess()
        val updatedDb = currentDb.updateEntry(updatedEntry)
        
        return saveDatabase(uri, updatedDb)
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
        return exportDatabase(backupUri)
    }
    
    /**
     * Exports the current database to an external file.
     */
    suspend fun exportDatabase(exportUri: Uri): Result<Unit> {
        val dbFile = internalDbFile ?: return Result.failure(Exception("No database loaded"))
        
        return try {
            val outputStream = context.contentResolver.openOutputStream(exportUri, "wt")
                ?: return Result.failure(Exception("Cannot open file for writing"))
            
            dbFile.inputStream().use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }
            
            Log.d(TAG, "Database exported to $exportUri")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to export database: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    /**
     * Imports a database from an external file (backup).
     * This copies the file to internal storage but doesn't unlock it.
     * User will need to restart and enter the backup's master password.
     */
    suspend fun importDatabase(importUri: Uri): Result<Unit> {
        return try {
            // Close current database
            lockVault()
            
            // Get internal storage file
            val dbFile = getInternalDbFile("current")
            
            // Copy the imported file to internal storage
            val inputStream = context.contentResolver.openInputStream(importUri)
                ?: return Result.failure(Exception("Cannot open backup file"))
            
            inputStream.use { input ->
                FileOutputStream(dbFile).use { output ->
                    input.copyTo(output)
                }
            }
            
            Log.d(TAG, "Database imported from $importUri, file size: ${dbFile.length()}")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to import database: ${e.message}", e)
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
            // Use internal file for reliable saving
            val dbFile = internalDbFile ?: getInternalDbFile("current")
            val outputStream = FileOutputStream(dbFile)
            
            Log.d(TAG, "Saving database to: ${dbFile.absolutePath}")
            
            val result = databaseHandler.saveDatabase(outputStream, database)
            
            result.fold(
                onSuccess = {
                    Log.d(TAG, "Database saved, file size: ${dbFile.length()}")
                    internalDbFile = dbFile
                    _database.value = database
                    Result.success(Unit)
                },
                onFailure = { 
                    Log.e(TAG, "Failed to save database: ${it.message}")
                    Result.failure(it) 
                }
            )
        } catch (e: Exception) {
            Log.e(TAG, "Exception saving database: ${e.message}", e)
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
