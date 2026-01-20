package com.pesky.app.data.models

import java.time.Instant
import java.util.UUID

/**
 * Represents a group (folder/category) in the vault hierarchy.
 */
data class Group(
    val uuid: String = UUID.randomUUID().toString(),
    val name: String,
    val iconId: Int = 0,
    val parentUuid: String? = null,
    val times: GroupTimes = GroupTimes(),
    val isExpanded: Boolean = true
) {
    companion object {
        // Default root group
        val ROOT = Group(
            uuid = "root",
            name = "Root",
            iconId = 0,
            parentUuid = null
        )
    }
}

/**
 * Represents timestamps for a group.
 */
data class GroupTimes(
    val creationTime: Instant = Instant.now(),
    val lastModificationTime: Instant = Instant.now()
)

/**
 * Represents a deleted object for tracking purposes.
 */
data class DeletedObject(
    val uuid: String,
    val deletionTime: Instant = Instant.now()
)
