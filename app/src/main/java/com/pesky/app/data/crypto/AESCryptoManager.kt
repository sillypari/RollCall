package com.pesky.app.data.crypto

import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Handles AES-256-CBC encryption and decryption operations.
 * FIPS 140-2 compliant encryption for database security.
 */
@Singleton
class AESCryptoManager @Inject constructor() {
    
    companion object {
        private const val ALGORITHM = "AES"
        private const val TRANSFORMATION = "AES/CBC/PKCS5Padding"
        private const val IV_LENGTH = 16 // 128 bits
        private const val KEY_LENGTH = 32 // 256 bits
    }
    
    private val secureRandom = SecureRandom()
    
    /**
     * Encrypts data using AES-256-CBC.
     * 
     * @param plaintext The data to encrypt
     * @param key The 256-bit encryption key
     * @param iv The 16-byte initialization vector
     * @return The encrypted ciphertext
     */
    fun encrypt(plaintext: ByteArray, key: ByteArray, iv: ByteArray): ByteArray {
        require(key.size == KEY_LENGTH) { "Key must be $KEY_LENGTH bytes (256 bits)" }
        require(iv.size == IV_LENGTH) { "IV must be $IV_LENGTH bytes (128 bits)" }
        
        val secretKey = SecretKeySpec(key, ALGORITHM)
        val ivSpec = IvParameterSpec(iv)
        
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec)
        
        return cipher.doFinal(plaintext)
    }
    
    /**
     * Decrypts data using AES-256-CBC.
     * 
     * @param ciphertext The encrypted data
     * @param key The 256-bit decryption key
     * @param iv The 16-byte initialization vector
     * @return The decrypted plaintext
     */
    fun decrypt(ciphertext: ByteArray, key: ByteArray, iv: ByteArray): ByteArray {
        require(key.size == KEY_LENGTH) { "Key must be $KEY_LENGTH bytes (256 bits)" }
        require(iv.size == IV_LENGTH) { "IV must be $IV_LENGTH bytes (128 bits)" }
        
        val secretKey = SecretKeySpec(key, ALGORITHM)
        val ivSpec = IvParameterSpec(iv)
        
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec)
        
        return cipher.doFinal(ciphertext)
    }
    
    /**
     * Generates a cryptographically secure random IV.
     * 
     * @return 16 bytes of random IV
     */
    fun generateIV(): ByteArray {
        val iv = ByteArray(IV_LENGTH)
        secureRandom.nextBytes(iv)
        return iv
    }
    
    /**
     * Generates a cryptographically secure random master seed.
     * 
     * @return 32 bytes of random seed
     */
    fun generateMasterSeed(): ByteArray {
        val seed = ByteArray(KEY_LENGTH)
        secureRandom.nextBytes(seed)
        return seed
    }
}
