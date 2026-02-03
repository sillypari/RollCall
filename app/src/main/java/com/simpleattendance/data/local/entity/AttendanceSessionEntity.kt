package com.simpleattendance.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "attendance_sessions",
    foreignKeys = [
        ForeignKey(
            entity = ClassEntity::class,
            parentColumns = ["id"],
            childColumns = ["classId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("classId")]
)
data class AttendanceSessionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val classId: Long,
    val date: Long = System.currentTimeMillis(),
    val presentCount: Int = 0,
    val absentCount: Int = 0,
    val totalCount: Int = 0
) {
    val percentage: Float
        get() = if (totalCount > 0) (presentCount.toFloat() / totalCount) * 100 else 0f
}
