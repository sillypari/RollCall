package com.pesky.app.data.crypto

import javax.inject.Inject
import javax.inject.Singleton

/**
 * Orchestrates all cryptographic operations for the Pesky database.
 * Combines key derivation, encryption, compression, and HMAC validation.
 */
@Singleton
class DatabaseCryptoManager @Inject constructor(
    private val argon2KeyDerivation: Argon2KeyDerivation,
    private val aesCryptoManager: AESCryptoManager,
    private val hmacValidator: HMACValidator,
    private val compressionManager: CompressionManager
) {
    
    /**
     * Encrypts database payload for storage.
     * Process: XML → GZIP compress → AES-256-CBC encrypt
     * 
     * @param xmlPayload The XML content to encrypt
     * @param encryptionKey The derived encryption key (32 bytes)
     * @param iv The initialization vector (16 bytes)
     * @return The encrypted payload
     */
    fun encryptPayload(xmlPayload: ByteArray, encryptionKey: ByteArray, iv: ByteArray): ByteArray {
        // Compress first, then encrypt
        val compressed = compressionManager.compress(xmlPayload)
        return aesCryptoManager.encrypt(compressed, encryptionKey, iv)
    }
    
    /**
     * Decrypts database payload.
     * Process: AES-256-CBC decrypt → GZIP decompress → XML
     * 
     * @param encryptedPayload The encrypted data
     * @param encryptionKey The derived encryption key (32 bytes)
     * @param iv The initialization vector (16 bytes)
     * @return The decrypted XML payload
     */
    fun decryptPayload(encryptedPayload: ByteArray, encryptionKey: ByteArray, iv: ByteArray): ByteArray {
        // Decrypt first, then decompress
        val decrypted = aesCryptoManager.decrypt(encryptedPayload, encryptionKey, iv)
        return compressionManager.decompress(decrypted)
    }
    
    /**
     * Derives encryption key from master password.
     * 
     * @param masterPassword The master password (cleared after use)
     * @param salt The salt bytes
     * @return The derived 256-bit encryption key
     */
    fun deriveEncryptionKey(masterPassword: CharArray, salt: ByteArray): ByteArray {
        return argon2KeyDerivation.deriveKey(masterPassword, salt)
    }
    
    /**
     * Computes HMAC for integrity verification.
     * 
     * @param data The encrypted payload
     * @param hmacKey The HMAC key
     * @return The HMAC-SHA256 signature
     */
    fun computeHMAC(data: ByteArray, hmacKey: ByteArray): ByteArray {
        return hmacValidator.computeHMAC(data, hmacKey)
    }
    
    /**
     * Verifies HMAC signature for tamper detection.
     * 
     * @param data The encrypted payload
     * @param hmacKey The HMAC key
     * @param expectedHMAC The stored HMAC signature
     * @return True if data integrity is verified
     */
    fun verifyHMAC(data: ByteArray, hmacKey: ByteArray, expectedHMAC: ByteArray): Boolean {
        return hmacValidator.verifyHMAC(data, hmacKey, expectedHMAC)
    }
    
    /**
     * Generates all random values needed for a new database.
     * 
     * @return DatabaseCryptoParams containing all generated values
     */
    fun generateDatabaseParams(): DatabaseCryptoParams {
        return DatabaseCryptoParams(
            masterSeed = aesCryptoManager.generateMasterSeed(),
            iv = aesCryptoManager.generateIV(),
            salt = argon2KeyDerivation.generateSalt(),
            hmacKey = hmacValidator.generateHMACKey()
        )
    }
    
    /**
     * Securely clears sensitive data from memory.
     */
    fun clearSensitiveData(vararg arrays: ByteArray) {
        arrays.forEach { it.fill(0) }
    }
    
    fun clearPassword(password: CharArray) {
        password.fill('\u0000')
    }
}

/**
 * Container for cryptographic parameters used by the database.
 */
data class DatabaseCryptoParams(
    val masterSeed: ByteArray,  // 32 bytes
    val iv: ByteArray,          // 16 bytes
    val salt: ByteArray,        // 32 bytes
    val hmacKey: ByteArray      // 32 bytes
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        
        other as DatabaseCryptoParams
        
        if (!masterSeed.contentEquals(other.masterSeed)) return false
        if (!iv.contentEquals(other.iv)) return false
        if (!salt.contentEquals(other.salt)) return false
        if (!hmacKey.contentEquals(other.hmacKey)) return false
        
        return true
    }
    
    override fun hashCode(): Int {
        var result = masterSeed.contentHashCode()
        result = 31 * result + iv.contentHashCode()
        result = 31 * result + salt.contentHashCode()
        result = 31 * result + hmacKey.contentHashCode()
        return result
    }
    
    /**
     * Securely clears all sensitive data from memory.
     */
    fun clear() {
        masterSeed.fill(0)
        iv.fill(0)
        salt.fill(0)
        hmacKey.fill(0)
    }
}
