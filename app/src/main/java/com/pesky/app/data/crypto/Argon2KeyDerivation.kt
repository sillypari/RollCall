package com.pesky.app.data.crypto

import org.bouncycastle.crypto.generators.Argon2BytesGenerator
import org.bouncycastle.crypto.params.Argon2Parameters
import java.security.SecureRandom
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Handles key derivation using Argon2id algorithm.
 * Argon2id is resistant to GPU attacks and side-channel attacks.
 */
@Singleton
class Argon2KeyDerivation @Inject constructor() {
    
    companion object {
        // Argon2id parameters for strong security
        private const val ITERATIONS = 100_000
        private const val MEMORY_KB = 65536 // 64 MB
        private const val PARALLELISM = 4
        private const val KEY_LENGTH = 32 // 256 bits
        private const val SALT_LENGTH = 32
    }
    
    private val secureRandom = SecureRandom()
    
    /**
     * Derives a 256-bit key from the master password using Argon2id.
     * 
     * @param password The master password as CharArray (cleared after use)
     * @param salt The salt bytes (32 bytes)
     * @return The derived key (32 bytes / 256 bits)
     */
    fun deriveKey(password: CharArray, salt: ByteArray): ByteArray {
        require(salt.size == SALT_LENGTH) { "Salt must be $SALT_LENGTH bytes" }
        
        val passwordBytes = charArrayToBytes(password)
        
        try {
            val params = Argon2Parameters.Builder(Argon2Parameters.ARGON2_id)
                .withVersion(Argon2Parameters.ARGON2_VERSION_13)
                .withIterations(ITERATIONS)
                .withMemoryAsKB(MEMORY_KB)
                .withParallelism(PARALLELISM)
                .withSalt(salt)
                .build()
            
            val generator = Argon2BytesGenerator()
            generator.init(params)
            
            val derivedKey = ByteArray(KEY_LENGTH)
            generator.generateBytes(passwordBytes, derivedKey)
            
            return derivedKey
        } finally {
            // Clear sensitive data from memory
            passwordBytes.fill(0)
        }
    }
    
    /**
     * Generates a cryptographically secure random salt.
     * 
     * @return 32 bytes of random salt
     */
    fun generateSalt(): ByteArray {
        val salt = ByteArray(SALT_LENGTH)
        secureRandom.nextBytes(salt)
        return salt
    }
    
    /**
     * Converts CharArray to ByteArray using UTF-8 encoding.
     * The caller is responsible for clearing the returned byte array.
     */
    private fun charArrayToBytes(chars: CharArray): ByteArray {
        val charBuffer = java.nio.CharBuffer.wrap(chars)
        val byteBuffer = Charsets.UTF_8.encode(charBuffer)
        val bytes = ByteArray(byteBuffer.remaining())
        byteBuffer.get(bytes)
        
        // Clear the buffer
        byteBuffer.array().fill(0)
        
        return bytes
    }
}
