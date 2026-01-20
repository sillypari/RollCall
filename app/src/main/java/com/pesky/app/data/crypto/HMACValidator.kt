package com.pesky.app.data.crypto

import java.security.SecureRandom
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Handles HMAC-SHA256 computation for message authentication and integrity verification.
 */
@Singleton
class HMACValidator @Inject constructor() {
    
    companion object {
        private const val ALGORITHM = "HmacSHA256"
        private const val HMAC_KEY_LENGTH = 32 // 256 bits
    }
    
    private val secureRandom = SecureRandom()
    
    /**
     * Computes HMAC-SHA256 of the given data.
     * 
     * @param data The data to authenticate
     * @param key The HMAC key (32 bytes)
     * @return The HMAC-SHA256 signature (32 bytes)
     */
    fun computeHMAC(data: ByteArray, key: ByteArray): ByteArray {
        require(key.size == HMAC_KEY_LENGTH) { "HMAC key must be $HMAC_KEY_LENGTH bytes" }
        
        val secretKey = SecretKeySpec(key, ALGORITHM)
        val mac = Mac.getInstance(ALGORITHM)
        mac.init(secretKey)
        
        return mac.doFinal(data)
    }
    
    /**
     * Verifies the HMAC-SHA256 signature of the given data.
     * Uses constant-time comparison to prevent timing attacks.
     * 
     * @param data The data to verify
     * @param key The HMAC key (32 bytes)
     * @param expectedHMAC The expected HMAC signature
     * @return True if the signature is valid, false otherwise
     */
    fun verifyHMAC(data: ByteArray, key: ByteArray, expectedHMAC: ByteArray): Boolean {
        val computedHMAC = computeHMAC(data, key)
        return constantTimeEquals(computedHMAC, expectedHMAC)
    }
    
    /**
     * Generates a cryptographically secure random HMAC key.
     * 
     * @return 32 bytes of random HMAC key
     */
    fun generateHMACKey(): ByteArray {
        val key = ByteArray(HMAC_KEY_LENGTH)
        secureRandom.nextBytes(key)
        return key
    }
    
    /**
     * Constant-time comparison to prevent timing attacks.
     */
    private fun constantTimeEquals(a: ByteArray, b: ByteArray): Boolean {
        if (a.size != b.size) return false
        
        var result = 0
        for (i in a.indices) {
            result = result or (a[i].toInt() xor b[i].toInt())
        }
        return result == 0
    }
}
