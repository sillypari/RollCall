package com.simpleattendance.data.local.dao

import androidx.room.*
import com.simpleattendance.data.local.entity.AttendanceSessionEntity
import com.simpleattendance.data.local.entity.AttendanceRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AttendanceDao {
    // Session queries
    @Query("SELECT * FROM attendance_sessions ORDER BY date DESC")
    fun getAllSessions(): Flow<List<AttendanceSessionEntity>>
    
    @Query("SELECT * FROM attendance_sessions WHERE classId = :classId ORDER BY date DESC")
    fun getSessionsByClass(classId: Long): Flow<List<AttendanceSessionEntity>>
    
    @Query("SELECT * FROM attendance_sessions WHERE id = :id")
    suspend fun getSessionById(id: Long): AttendanceSessionEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: AttendanceSessionEntity): Long
    
    @Update
    suspend fun updateSession(session: AttendanceSessionEntity)
    
    @Delete
    suspend fun deleteSession(session: AttendanceSessionEntity)
    
    // Record queries
    @Query("SELECT * FROM attendance_records WHERE sessionId = :sessionId")
    suspend fun getRecordsBySession(sessionId: Long): List<AttendanceRecordEntity>
    
    @Query("SELECT * FROM attendance_records WHERE sessionId = :sessionId AND status = 'P'")
    suspend fun getPresentRecords(sessionId: Long): List<AttendanceRecordEntity>
    
    @Query("SELECT * FROM attendance_records WHERE sessionId = :sessionId AND status = 'A'")
    suspend fun getAbsentRecords(sessionId: Long): List<AttendanceRecordEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: AttendanceRecordEntity): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecords(records: List<AttendanceRecordEntity>)
    
    @Update
    suspend fun updateRecord(record: AttendanceRecordEntity)
    
    @Delete
    suspend fun deleteRecord(record: AttendanceRecordEntity)
    
    @Query("DELETE FROM attendance_records WHERE sessionId = :sessionId")
    suspend fun deleteRecordsBySession(sessionId: Long)
}
