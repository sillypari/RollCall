package com.pesky.app.data.database

import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * Represents the unencrypted header of a .pesky file.
 * 
 * Header Structure (96 bytes total):
 * - Magic Signature: 4 bytes (0x9AA2D903)
 * - Version: 4 bytes (0x00040001 for v4.1)
 * - Encryption Algorithm ID: 4 bytes (1 = AES-256)
 * - Key Derivation ID: 4 bytes (1 = Argon2id)
 * - Master Seed: 32 bytes
 * - Initialization Vector: 16 bytes
 * - Salt: 32 bytes
 */
data class DatabaseHeader(
    val version: Int = VERSION_4_1,
    val encryptionAlgorithm: EncryptionAlgorithm = EncryptionAlgorithm.AES_256,
    val keyDerivation: KeyDerivation = KeyDerivation.ARGON2ID,
    val masterSeed: ByteArray,
    val iv: ByteArray,
    val salt: ByteArray
) {
    companion object {
        const val MAGIC_SIGNATURE = 0x9AA2D903.toInt()
        const val VERSION_4_1 = 0x00040001
        const val HEADER_SIZE = 96
        
        // Sizes
        const val MASTER_SEED_SIZE = 32
        const val IV_SIZE = 16
        const val SALT_SIZE = 32
        
        /**
         * Parses a header from raw bytes.
         */
        fun fromBytes(bytes: ByteArray): Result<DatabaseHeader> {
            if (bytes.size < HEADER_SIZE) {
                return Result.failure(InvalidHeaderException("Header too small: ${bytes.size} bytes"))
            }
            
            val buffer = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN)
            
            // Read and verify magic signature
            val magic = buffer.int
            if (magic != MAGIC_SIGNATURE) {
                return Result.failure(InvalidHeaderException("Invalid magic signature: 0x${magic.toString(16)}"))
            }
            
            // Read version
            val version = buffer.int
            if (version != VERSION_4_1) {
                return Result.failure(UnsupportedVersionException("Unsupported version: 0x${version.toString(16)}"))
            }
            
            // Read algorithm IDs
            val encryptionId = buffer.int
            val keyDerivationId = buffer.int
            
            val encryptionAlgorithm = EncryptionAlgorithm.fromId(encryptionId)
                ?: return Result.failure(InvalidHeaderException("Unknown encryption algorithm: $encryptionId"))
            
            val keyDerivation = KeyDerivation.fromId(keyDerivationId)
                ?: return Result.failure(InvalidHeaderException("Unknown key derivation: $keyDerivationId"))
            
            // Read cryptographic material
            val masterSeed = ByteArray(MASTER_SEED_SIZE)
            buffer.get(masterSeed)
            
            val iv = ByteArray(IV_SIZE)
            buffer.get(iv)
            
            val salt = ByteArray(SALT_SIZE)
            buffer.get(salt)
            
            return Result.success(
                DatabaseHeader(
                    version = version,
                    encryptionAlgorithm = encryptionAlgorithm,
                    keyDerivation = keyDerivation,
                    masterSeed = masterSeed,
                    iv = iv,
                    salt = salt
                )
            )
        }
    }
    
    /**
     * Serializes the header to bytes.
     */
    fun toBytes(): ByteArray {
        val buffer = ByteBuffer.allocate(HEADER_SIZE).order(ByteOrder.LITTLE_ENDIAN)
        
        buffer.putInt(MAGIC_SIGNATURE)
        buffer.putInt(version)
        buffer.putInt(encryptionAlgorithm.id)
        buffer.putInt(keyDerivation.id)
        buffer.put(masterSeed)
        buffer.put(iv)
        buffer.put(salt)
        
        return buffer.array()
    }
    
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        
        other as DatabaseHeader
        
        if (version != other.version) return false
        if (encryptionAlgorithm != other.encryptionAlgorithm) return false
        if (keyDerivation != other.keyDerivation) return false
        if (!masterSeed.contentEquals(other.masterSeed)) return false
        if (!iv.contentEquals(other.iv)) return false
        if (!salt.contentEquals(other.salt)) return false
        
        return true
    }
    
    override fun hashCode(): Int {
        var result = version
        result = 31 * result + encryptionAlgorithm.hashCode()
        result = 31 * result + keyDerivation.hashCode()
        result = 31 * result + masterSeed.contentHashCode()
        result = 31 * result + iv.contentHashCode()
        result = 31 * result + salt.contentHashCode()
        return result
    }
}

/**
 * Supported encryption algorithms.
 */
enum class EncryptionAlgorithm(val id: Int, val displayName: String) {
    AES_256(1, "AES-256-CBC");
    
    companion object {
        fun fromId(id: Int): EncryptionAlgorithm? = values().find { it.id == id }
    }
}

/**
 * Supported key derivation functions.
 */
enum class KeyDerivation(val id: Int, val displayName: String) {
    ARGON2ID(1, "Argon2id");
    
    companion object {
        fun fromId(id: Int): KeyDerivation? = values().find { it.id == id }
    }
}

/**
 * Exception for invalid header format.
 */
class InvalidHeaderException(message: String) : Exception(message)

/**
 * Exception for unsupported database version.
 */
class UnsupportedVersionException(message: String) : Exception(message)
