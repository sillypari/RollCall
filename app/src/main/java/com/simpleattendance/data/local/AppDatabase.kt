package com.simpleattendance.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.simpleattendance.data.local.dao.ClassDao
import com.simpleattendance.data.local.dao.StudentDao
import com.simpleattendance.data.local.dao.AttendanceDao
import com.simpleattendance.data.local.entity.ClassEntity
import com.simpleattendance.data.local.entity.StudentEntity
import com.simpleattendance.data.local.entity.AttendanceSessionEntity
import com.simpleattendance.data.local.entity.AttendanceRecordEntity

@Database(
    entities = [
        ClassEntity::class,
        StudentEntity::class,
        AttendanceSessionEntity::class,
        AttendanceRecordEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun classDao(): ClassDao
    abstract fun studentDao(): StudentDao
    abstract fun attendanceDao(): AttendanceDao
}
