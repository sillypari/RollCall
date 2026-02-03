package com.simpleattendance.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "classes")
data class ClassEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val branch: String,
    val semester: String,
    val section: String,
    val subject: String,
    val createdAt: Long = System.currentTimeMillis()
) {
    val displayName: String
        get() = "$branch $semester-$section"
    
    val fullDisplayName: String
        get() = "$branch $semester-$section ($subject)"
    
    val batchKey: String
        get() = "$branch|$semester|$section"
}
