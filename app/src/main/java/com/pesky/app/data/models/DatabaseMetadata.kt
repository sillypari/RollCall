package com.pesky.app.data.models

import java.time.Instant

/**
 * Represents the metadata section of a Pesky database.
 */
data class DatabaseMetadata(
    val version: String = "4.1.0",
    val databaseName: String = "My Passwords",
    val databaseDescription: String = "Created with Pesky",
    val creationTime: Instant = Instant.now(),
    val lastModificationTime: Instant = Instant.now(),
    val generator: String = "Pesky Android",
    val entryCount: Int = 0,
    val groupCount: Int = 0
)

/**
 * Represents the complete vault database structure.
 */
data class VaultDatabase(
    val metadata: DatabaseMetadata = DatabaseMetadata(),
    val groups: List<Group> = listOf(Group.ROOT),
    val entries: List<PasswordEntry> = emptyList(),
    val deletedObjects: List<DeletedObject> = emptyList()
) {
    /**
     * Gets entries for a specific group.
     */
    fun getEntriesForGroup(groupUuid: String?): List<PasswordEntry> {
        return entries.filter { it.groupUuid == groupUuid }
    }
    
    /**
     * Gets all favorite entries.
     */
    fun getFavorites(): List<PasswordEntry> {
        return entries.filter { it.isFavorite }
    }
    
    /**
     * Gets recently used entries (last 10).
     */
    fun getRecentlyUsed(limit: Int = 10): List<PasswordEntry> {
        return entries
            .sortedByDescending { it.times.lastAccessTime }
            .take(limit)
    }
    
    /**
     * Gets entries with weak passwords.
     */
    fun getWeakPasswordEntries(analyzer: (String) -> PasswordStrength): List<PasswordEntry> {
        return entries.filter { entry ->
            val strength = analyzer(entry.password)
            strength == PasswordStrength.VERY_WEAK || strength == PasswordStrength.WEAK
        }
    }
    
    /**
     * Gets entries with duplicate passwords.
     */
    fun getDuplicatePasswordEntries(): List<PasswordEntry> {
        val passwordCounts = entries
            .filter { it.password.isNotEmpty() }
            .groupBy { it.password }
            .filterValues { it.size > 1 }
        
        return passwordCounts.values.flatten()
    }
    
    /**
     * Gets entries expiring soon.
     */
    fun getExpiringSoonEntries(daysThreshold: Int = 30): List<PasswordEntry> {
        return entries.filter { it.times.isExpiringSoon(daysThreshold) }
    }
    
    /**
     * Gets secure notes (entries with notes but minimal password).
     */
    fun getSecureNotes(): List<PasswordEntry> {
        return entries.filter { 
            it.notes.isNotEmpty() && it.password.isEmpty() && it.userName.isEmpty() 
        }
    }
    
    /**
     * Searches entries by query.
     */
    fun search(query: String): List<PasswordEntry> {
        if (query.isBlank()) return entries
        
        val lowerQuery = query.lowercase()
        return entries.filter { entry ->
            entry.title.lowercase().contains(lowerQuery) ||
            entry.userName.lowercase().contains(lowerQuery) ||
            entry.url.lowercase().contains(lowerQuery) ||
            entry.notes.lowercase().contains(lowerQuery) ||
            entry.tags.any { it.lowercase().contains(lowerQuery) }
        }
    }
    
    /**
     * Gets the count of entries by category.
     */
    fun getCategoryCounts(): Map<String, Int> {
        return groups.associate { group ->
            group.name to entries.count { it.groupUuid == group.uuid }
        }
    }
    
    /**
     * Adds a new entry to the database.
     */
    fun addEntry(entry: PasswordEntry): VaultDatabase {
        return copy(
            entries = entries + entry,
            metadata = metadata.copy(
                lastModificationTime = Instant.now(),
                entryCount = entries.size + 1
            )
        )
    }
    
    /**
     * Updates an existing entry.
     */
    fun updateEntry(entry: PasswordEntry): VaultDatabase {
        val updatedEntries = entries.map { 
            if (it.uuid == entry.uuid) entry else it 
        }
        return copy(
            entries = updatedEntries,
            metadata = metadata.copy(lastModificationTime = Instant.now())
        )
    }
    
    /**
     * Deletes an entry (moves to deleted objects).
     */
    fun deleteEntry(entryUuid: String): VaultDatabase {
        val entryToDelete = entries.find { it.uuid == entryUuid } ?: return this
        return copy(
            entries = entries.filter { it.uuid != entryUuid },
            deletedObjects = deletedObjects + DeletedObject(entryUuid),
            metadata = metadata.copy(
                lastModificationTime = Instant.now(),
                entryCount = entries.size - 1
            )
        )
    }
    
    /**
     * Adds a new group.
     */
    fun addGroup(group: Group): VaultDatabase {
        return copy(
            groups = groups + group,
            metadata = metadata.copy(
                lastModificationTime = Instant.now(),
                groupCount = groups.size + 1
            )
        )
    }
    
    /**
     * Gets group by UUID.
     */
    fun getGroup(uuid: String): Group? {
        return groups.find { it.uuid == uuid }
    }
}
