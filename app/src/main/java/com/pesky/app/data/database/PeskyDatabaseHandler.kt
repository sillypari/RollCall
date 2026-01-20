package com.pesky.app.data.database

import android.net.Uri
import com.pesky.app.data.crypto.DatabaseCryptoManager
import com.pesky.app.data.crypto.DatabaseCryptoParams
import com.pesky.app.data.models.VaultDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Handles reading and writing .pesky database files.
 * 
 * File Structure:
 * - Header (96 bytes, unencrypted)
 * - HMAC Key (32 bytes, encrypted with master seed)
 * - HMAC Signature (32 bytes)
 * - Encrypted Payload (variable length)
 */
@Singleton
class PeskyDatabaseHandler @Inject constructor(
    private val cryptoManager: DatabaseCryptoManager,
    private val xmlParser: XMLParser
) {
    
    companion object {
        const val FILE_EXTENSION = ".pesky"
        private const val HMAC_SIZE = 32
    }
    
    private val mutex = Mutex()
    
    // In-memory cache of decrypted database
    private var cachedDatabase: VaultDatabase? = null
    private var cachedFilePath: String? = null
    private var cachedEncryptionKey: ByteArray? = null
    private var cachedHeader: DatabaseHeader? = null
    private var cachedHmacKey: ByteArray? = null
    
    /**
     * Creates a new .pesky database file.
     * 
     * @param outputStream The output stream to write to
     * @param masterPassword The master password for encryption
     * @param databaseName The name for the new database
     * @return The created VaultDatabase
     */
    suspend fun createDatabase(
        outputStream: OutputStream,
        masterPassword: CharArray,
        databaseName: String = "My Passwords"
    ): Result<VaultDatabase> = withContext(Dispatchers.IO) {
        mutex.withLock {
            try {
                // Generate cryptographic parameters
                val cryptoParams = cryptoManager.generateDatabaseParams()
                
                // Derive encryption key from master password
                val encryptionKey = cryptoManager.deriveEncryptionKey(masterPassword, cryptoParams.salt)
                
                // Create empty database
                val database = VaultDatabase(
                    metadata = com.pesky.app.data.models.DatabaseMetadata(databaseName = databaseName)
                )
                
                // Create header
                val header = DatabaseHeader(
                    masterSeed = cryptoParams.masterSeed,
                    iv = cryptoParams.iv,
                    salt = cryptoParams.salt
                )
                
                // Generate XML
                val xmlPayload = xmlParser.generateXML(database)
                
                // Encrypt payload
                val encryptedPayload = cryptoManager.encryptPayload(xmlPayload, encryptionKey, cryptoParams.iv)
                
                // Compute HMAC of encrypted payload
                val hmac = cryptoManager.computeHMAC(encryptedPayload, cryptoParams.hmacKey)
                
                // Encrypt HMAC key with master seed (for storage)
                val encryptedHmacKey = cryptoManager.encryptPayload(
                    cryptoParams.hmacKey, 
                    encryptionKey, 
                    cryptoParams.iv
                )
                
                // Write file
                outputStream.use { stream ->
                    // Write header
                    stream.write(header.toBytes())
                    
                    // Write encrypted HMAC key length + data
                    val hmacKeyLengthBuffer = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN)
                    hmacKeyLengthBuffer.putInt(encryptedHmacKey.size)
                    stream.write(hmacKeyLengthBuffer.array())
                    stream.write(encryptedHmacKey)
                    
                    // Write HMAC signature
                    stream.write(hmac)
                    
                    // Write encrypted payload length + data
                    val payloadLengthBuffer = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN)
                    payloadLengthBuffer.putInt(encryptedPayload.size)
                    stream.write(payloadLengthBuffer.array())
                    stream.write(encryptedPayload)
                }
                
                // Cache for future operations
                cachedDatabase = database
                cachedEncryptionKey = encryptionKey.copyOf()
                cachedHeader = header
                cachedHmacKey = cryptoParams.hmacKey.copyOf()
                
                // Clear sensitive data
                cryptoParams.clear()
                cryptoManager.clearPassword(masterPassword)
                
                Result.success(database)
            } catch (e: Exception) {
                Result.failure(DatabaseException("Failed to create database: ${e.message}", e))
            }
        }
    }
    
    /**
     * Opens an existing .pesky database file.
     * 
     * @param inputStream The input stream to read from
     * @param masterPassword The master password for decryption
     * @param filePath Optional file path for caching purposes
     * @return The decrypted VaultDatabase
     */
    suspend fun openDatabase(
        inputStream: InputStream,
        masterPassword: CharArray,
        filePath: String? = null
    ): Result<VaultDatabase> = withContext(Dispatchers.IO) {
        mutex.withLock {
            try {
                val fileBytes = inputStream.use { it.readBytes() }
                
                if (fileBytes.size < DatabaseHeader.HEADER_SIZE + 4 + HMAC_SIZE + 4) {
                    return@withContext Result.failure(DatabaseException("File too small to be a valid database"))
                }
                
                // Parse header
                val headerBytes = fileBytes.sliceArray(0 until DatabaseHeader.HEADER_SIZE)
                val header = DatabaseHeader.fromBytes(headerBytes).getOrElse { 
                    return@withContext Result.failure(it) 
                }
                
                var offset = DatabaseHeader.HEADER_SIZE
                
                // Read encrypted HMAC key
                val hmacKeyLength = ByteBuffer.wrap(fileBytes, offset, 4)
                    .order(ByteOrder.LITTLE_ENDIAN).int
                offset += 4
                val encryptedHmacKey = fileBytes.sliceArray(offset until offset + hmacKeyLength)
                offset += hmacKeyLength
                
                // Read stored HMAC
                val storedHmac = fileBytes.sliceArray(offset until offset + HMAC_SIZE)
                offset += HMAC_SIZE
                
                // Read encrypted payload
                val payloadLength = ByteBuffer.wrap(fileBytes, offset, 4)
                    .order(ByteOrder.LITTLE_ENDIAN).int
                offset += 4
                val encryptedPayload = fileBytes.sliceArray(offset until offset + payloadLength)
                
                // Derive encryption key
                val encryptionKey = cryptoManager.deriveEncryptionKey(masterPassword, header.salt)
                
                // Decrypt HMAC key
                val hmacKey = try {
                    cryptoManager.decryptPayload(encryptedHmacKey, encryptionKey, header.iv)
                } catch (e: Exception) {
                    cryptoManager.clearSensitiveData(encryptionKey)
                    return@withContext Result.failure(WrongPasswordException("Incorrect master password"))
                }
                
                // Verify HMAC
                if (!cryptoManager.verifyHMAC(encryptedPayload, hmacKey, storedHmac)) {
                    cryptoManager.clearSensitiveData(encryptionKey, hmacKey)
                    return@withContext Result.failure(IntegrityException("Database integrity check failed"))
                }
                
                // Decrypt payload
                val xmlPayload = try {
                    cryptoManager.decryptPayload(encryptedPayload, encryptionKey, header.iv)
                } catch (e: Exception) {
                    cryptoManager.clearSensitiveData(encryptionKey, hmacKey)
                    return@withContext Result.failure(WrongPasswordException("Incorrect master password"))
                }
                
                // Parse XML
                val database = xmlParser.parseXML(xmlPayload).getOrElse {
                    cryptoManager.clearSensitiveData(encryptionKey, hmacKey)
                    return@withContext Result.failure(it)
                }
                
                // Cache for future operations
                cachedDatabase = database
                cachedFilePath = filePath
                cachedEncryptionKey = encryptionKey.copyOf()
                cachedHeader = header
                cachedHmacKey = hmacKey.copyOf()
                
                // Clear password
                cryptoManager.clearPassword(masterPassword)
                
                Result.success(database)
            } catch (e: Exception) {
                Result.failure(DatabaseException("Failed to open database: ${e.message}", e))
            }
        }
    }
    
    /**
     * Saves the current database to a file.
     * 
     * @param outputStream The output stream to write to
     * @param database The database to save (uses cached if null)
     */
    suspend fun saveDatabase(
        outputStream: OutputStream,
        database: VaultDatabase? = null
    ): Result<Unit> = withContext(Dispatchers.IO) {
        mutex.withLock {
            try {
                val db = database ?: cachedDatabase
                    ?: return@withContext Result.failure(DatabaseException("No database loaded"))
                val encryptionKey = cachedEncryptionKey
                    ?: return@withContext Result.failure(DatabaseException("No encryption key available"))
                val header = cachedHeader
                    ?: return@withContext Result.failure(DatabaseException("No header available"))
                val hmacKey = cachedHmacKey
                    ?: return@withContext Result.failure(DatabaseException("No HMAC key available"))
                
                // Generate XML
                val xmlPayload = xmlParser.generateXML(db)
                
                // Encrypt payload
                val encryptedPayload = cryptoManager.encryptPayload(xmlPayload, encryptionKey, header.iv)
                
                // Compute HMAC
                val hmac = cryptoManager.computeHMAC(encryptedPayload, hmacKey)
                
                // Encrypt HMAC key
                val encryptedHmacKey = cryptoManager.encryptPayload(hmacKey, encryptionKey, header.iv)
                
                // Write file atomically (to temp file first, then rename)
                outputStream.use { stream ->
                    stream.write(header.toBytes())
                    
                    val hmacKeyLengthBuffer = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN)
                    hmacKeyLengthBuffer.putInt(encryptedHmacKey.size)
                    stream.write(hmacKeyLengthBuffer.array())
                    stream.write(encryptedHmacKey)
                    
                    stream.write(hmac)
                    
                    val payloadLengthBuffer = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN)
                    payloadLengthBuffer.putInt(encryptedPayload.size)
                    stream.write(payloadLengthBuffer.array())
                    stream.write(encryptedPayload)
                }
                
                // Update cache
                cachedDatabase = db
                
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(DatabaseException("Failed to save database: ${e.message}", e))
            }
        }
    }
    
    /**
     * Updates the cached database.
     */
    fun updateCachedDatabase(database: VaultDatabase) {
        cachedDatabase = database
    }
    
    /**
     * Gets the currently cached database.
     */
    fun getCachedDatabase(): VaultDatabase? = cachedDatabase
    
    /**
     * Checks if a database is currently loaded.
     */
    fun isDatabaseLoaded(): Boolean = cachedDatabase != null
    
    /**
     * Locks the vault by clearing all cached data.
     */
    fun lockVault() {
        cachedEncryptionKey?.fill(0)
        cachedHmacKey?.fill(0)
        cachedDatabase = null
        cachedFilePath = null
        cachedEncryptionKey = null
        cachedHeader = null
        cachedHmacKey = null
    }
    
    /**
     * Changes the master password.
     */
    suspend fun changeMasterPassword(
        outputStream: OutputStream,
        newPassword: CharArray
    ): Result<Unit> = withContext(Dispatchers.IO) {
        mutex.withLock {
            try {
                val database = cachedDatabase
                    ?: return@withContext Result.failure(DatabaseException("No database loaded"))
                val oldHeader = cachedHeader
                    ?: return@withContext Result.failure(DatabaseException("No header available"))
                val hmacKey = cachedHmacKey
                    ?: return@withContext Result.failure(DatabaseException("No HMAC key available"))
                
                // Generate new salt
                val newSalt = cryptoManager.generateDatabaseParams().salt
                
                // Derive new encryption key
                val newEncryptionKey = cryptoManager.deriveEncryptionKey(newPassword, newSalt)
                
                // Create new header with new salt
                val newHeader = oldHeader.copy(salt = newSalt)
                
                // Generate XML
                val xmlPayload = xmlParser.generateXML(database)
                
                // Encrypt with new key
                val encryptedPayload = cryptoManager.encryptPayload(xmlPayload, newEncryptionKey, newHeader.iv)
                
                // Compute HMAC
                val hmac = cryptoManager.computeHMAC(encryptedPayload, hmacKey)
                
                // Encrypt HMAC key with new key
                val encryptedHmacKey = cryptoManager.encryptPayload(hmacKey, newEncryptionKey, newHeader.iv)
                
                // Write file
                outputStream.use { stream ->
                    stream.write(newHeader.toBytes())
                    
                    val hmacKeyLengthBuffer = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN)
                    hmacKeyLengthBuffer.putInt(encryptedHmacKey.size)
                    stream.write(hmacKeyLengthBuffer.array())
                    stream.write(encryptedHmacKey)
                    
                    stream.write(hmac)
                    
                    val payloadLengthBuffer = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN)
                    payloadLengthBuffer.putInt(encryptedPayload.size)
                    stream.write(payloadLengthBuffer.array())
                    stream.write(encryptedPayload)
                }
                
                // Update cache with new key
                cachedEncryptionKey?.fill(0)
                cachedEncryptionKey = newEncryptionKey.copyOf()
                cachedHeader = newHeader
                
                // Clear password
                cryptoManager.clearPassword(newPassword)
                cryptoManager.clearSensitiveData(newEncryptionKey)
                
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(DatabaseException("Failed to change password: ${e.message}", e))
            }
        }
    }
    
    /**
     * Verifies database integrity without fully loading.
     */
    suspend fun verifyIntegrity(inputStream: InputStream, masterPassword: CharArray): Result<Boolean> = 
        withContext(Dispatchers.IO) {
            try {
                val result = openDatabase(inputStream, masterPassword)
                result.map { true }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
}

/**
 * General database exception.
 */
class DatabaseException(message: String, cause: Throwable? = null) : Exception(message, cause)

/**
 * Exception for wrong master password.
 */
class WrongPasswordException(message: String) : Exception(message)

/**
 * Exception for integrity check failure.
 */
class IntegrityException(message: String) : Exception(message)
