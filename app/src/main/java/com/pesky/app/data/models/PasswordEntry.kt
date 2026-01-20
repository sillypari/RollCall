package com.pesky.app.data.models

import java.time.Instant
import java.util.UUID

/**
 * Represents a password entry in the vault.
 */
data class PasswordEntry(
    val uuid: String = UUID.randomUUID().toString(),
    val title: String,
    val userName: String = "",
    val password: String = "",
    val url: String = "",
    val notes: String = "",
    val tags: List<String> = emptyList(),
    val iconId: Int = 0,
    val foregroundColor: String? = null,
    val groupUuid: String? = null,
    val isFavorite: Boolean = false,
    val customFields: List<CustomField> = emptyList(),
    val history: List<PasswordHistoryEntry> = emptyList(),
    val times: EntryTimes = EntryTimes(),
    val autoTypeEnabled: Boolean = false
) {
    /**
     * Creates a copy with the password updated and old password added to history.
     */
    fun withUpdatedPassword(newPassword: String): PasswordEntry {
        val historyEntry = PasswordHistoryEntry(
            password = this.password,
            modificationTime = Instant.now()
        )
        return copy(
            password = newPassword,
            history = history + historyEntry,
            times = times.copy(lastModificationTime = Instant.now())
        )
    }
    
    /**
     * Updates the last access time.
     */
    fun recordAccess(): PasswordEntry {
        return copy(times = times.copy(lastAccessTime = Instant.now()))
    }
}

/**
 * Represents timestamps for an entry.
 */
data class EntryTimes(
    val creationTime: Instant = Instant.now(),
    val lastModificationTime: Instant = Instant.now(),
    val lastAccessTime: Instant = Instant.now(),
    val expiryTime: Instant? = null,
    val expires: Boolean = false
) {
    fun isExpired(): Boolean {
        return expires && expiryTime != null && Instant.now().isAfter(expiryTime)
    }
    
    fun isExpiringSoon(daysThreshold: Int = 30): Boolean {
        if (!expires || expiryTime == null) return false
        val threshold = Instant.now().plusSeconds(daysThreshold * 24 * 60 * 60L)
        return expiryTime.isBefore(threshold) && !isExpired()
    }
}

/**
 * Represents a historical password entry.
 */
data class PasswordHistoryEntry(
    val password: String,
    val modificationTime: Instant
)

/**
 * Represents a custom field with optional protection (masking).
 */
data class CustomField(
    val key: String,
    val value: String,
    val isProtected: Boolean = false
)

/**
 * Represents the password strength level.
 */
enum class PasswordStrength(val score: Int, val color: Long) {
    VERY_WEAK(0, 0xFFFF453A),    // Red
    WEAK(1, 0xFFFF9F0A),          // Orange  
    FAIR(2, 0xFFFFD60A),          // Yellow
    STRONG(3, 0xFF32D74B),        // Green
    VERY_STRONG(4, 0xFF30D158)    // Bright Green
}

/**
 * Supported entry categories.
 */
enum class EntryCategory(val displayName: String, val iconId: Int) {
    SOCIAL_MEDIA("Social Media", 1),
    BANKING("Banking", 2),
    EMAIL("Email", 3),
    SHOPPING("Shopping", 4),
    WORK("Work", 5),
    ENTERTAINMENT("Entertainment", 6),
    GAMING("Gaming", 7),
    SECURE_NOTES("Secure Notes", 8),
    OTHER("Other", 0)
}
