package com.pesky.app.data.preferences

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages app preferences including quick unlock PIN and database state.
 */
@Singleton
class AppPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    private val prefs: SharedPreferences by lazy {
        try {
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()
            
            EncryptedSharedPreferences.create(
                context,
                "pesky_secure_prefs",
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: Exception) {
            // Fallback to regular prefs if encrypted fails
            context.getSharedPreferences("pesky_prefs", Context.MODE_PRIVATE)
        }
    }
    
    companion object {
        private const val KEY_HAS_DATABASE = "has_database"
        private const val KEY_DATABASE_NAME = "database_name"
        private const val KEY_QUICK_UNLOCK_ENABLED = "quick_unlock_enabled"
        private const val KEY_QUICK_UNLOCK_HASH = "quick_unlock_hash"
        private const val KEY_QUICK_UNLOCK_TYPE = "quick_unlock_type" // "pin" or "password"
        private const val KEY_AUTO_LOCK_TIMEOUT = "auto_lock_timeout"
        private const val KEY_BIOMETRIC_ENABLED = "biometric_enabled"
    }
    
    /**
     * Whether a database has been created/opened before.
     */
    var hasDatabase: Boolean
        get() = prefs.getBoolean(KEY_HAS_DATABASE, false)
        set(value) = prefs.edit().putBoolean(KEY_HAS_DATABASE, value).apply()
    
    /**
     * Name of the current database.
     */
    var databaseName: String
        get() = prefs.getString(KEY_DATABASE_NAME, "") ?: ""
        set(value) = prefs.edit().putString(KEY_DATABASE_NAME, value).apply()
    
    /**
     * Whether quick unlock (PIN/password) is enabled.
     */
    var quickUnlockEnabled: Boolean
        get() = prefs.getBoolean(KEY_QUICK_UNLOCK_ENABLED, false)
        set(value) = prefs.edit().putBoolean(KEY_QUICK_UNLOCK_ENABLED, value).apply()
    
    /**
     * Type of quick unlock: "pin" or "password"
     */
    var quickUnlockType: String
        get() = prefs.getString(KEY_QUICK_UNLOCK_TYPE, "pin") ?: "pin"
        set(value) = prefs.edit().putString(KEY_QUICK_UNLOCK_TYPE, value).apply()
    
    /**
     * Hash of the quick unlock PIN/password.
     */
    private var quickUnlockHash: String
        get() = prefs.getString(KEY_QUICK_UNLOCK_HASH, "") ?: ""
        set(value) = prefs.edit().putString(KEY_QUICK_UNLOCK_HASH, value).apply()
    
    /**
     * Auto-lock timeout in minutes. 0 = immediate, -1 = never.
     */
    var autoLockTimeout: Int
        get() = prefs.getInt(KEY_AUTO_LOCK_TIMEOUT, 5)
        set(value) = prefs.edit().putInt(KEY_AUTO_LOCK_TIMEOUT, value).apply()
    
    /**
     * Whether biometric unlock is enabled.
     */
    var biometricEnabled: Boolean
        get() = prefs.getBoolean(KEY_BIOMETRIC_ENABLED, false)
        set(value) = prefs.edit().putBoolean(KEY_BIOMETRIC_ENABLED, value).apply()
    
    /**
     * Set up a new quick unlock PIN/password.
     */
    fun setupQuickUnlock(pin: String, type: String = "pin") {
        quickUnlockHash = hashPin(pin)
        quickUnlockType = type
        quickUnlockEnabled = true
    }
    
    /**
     * Verify the quick unlock PIN/password.
     */
    fun verifyQuickUnlock(pin: String): Boolean {
        return quickUnlockHash == hashPin(pin)
    }
    
    /**
     * Remove quick unlock.
     */
    fun removeQuickUnlock() {
        quickUnlockEnabled = false
        quickUnlockHash = ""
    }
    
    /**
     * Mark that a database has been set up.
     */
    fun onDatabaseCreated(name: String) {
        hasDatabase = true
        databaseName = name
    }
    
    /**
     * Clear all preferences (for reset/logout).
     */
    fun clearAll() {
        prefs.edit().clear().apply()
    }
    
    /**
     * Hash a PIN/password using SHA-256.
     */
    private fun hashPin(pin: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(pin.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
