package com.simpleattendance.data.repository

import com.simpleattendance.data.local.dao.ClassDao
import com.simpleattendance.data.local.dao.StudentDao
import com.simpleattendance.data.local.dao.AttendanceDao
import com.simpleattendance.data.local.entity.ClassEntity
import com.simpleattendance.data.local.entity.StudentEntity
import com.simpleattendance.data.local.entity.AttendanceSessionEntity
import com.simpleattendance.data.local.entity.AttendanceRecordEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AttendanceRepository @Inject constructor(
    private val classDao: ClassDao,
    private val studentDao: StudentDao,
    private val attendanceDao: AttendanceDao
) {
    // Class operations
    fun getAllClasses(): Flow<List<ClassEntity>> = classDao.getAllClasses()
    
    suspend fun getClassById(id: Long): ClassEntity? = classDao.getClassById(id)
    
    suspend fun insertClass(classEntity: ClassEntity): Long = classDao.insertClass(classEntity)
    
    suspend fun updateClass(classEntity: ClassEntity) = classDao.updateClass(classEntity)
    
    suspend fun deleteClass(classEntity: ClassEntity) = classDao.deleteClass(classEntity)
    
    suspend fun deleteClassById(id: Long) = classDao.deleteClassById(id)
    
    // Student operations
    fun getStudentsByClass(classId: Long): Flow<List<StudentEntity>> = studentDao.getStudentsByClass(classId)
    
    suspend fun getStudentsByClassSync(classId: Long): List<StudentEntity> = studentDao.getStudentsByClassSync(classId)
    
    suspend fun insertStudents(students: List<StudentEntity>) = studentDao.insertStudents(students)
    
    suspend fun deleteStudentsByClass(classId: Long) = studentDao.deleteStudentsByClass(classId)
    
    // Session operations
    fun getAllSessions(): Flow<List<AttendanceSessionEntity>> = attendanceDao.getAllSessions()
    
    fun getSessionsByClass(classId: Long): Flow<List<AttendanceSessionEntity>> = attendanceDao.getSessionsByClass(classId)
    
    suspend fun getSessionById(id: Long): AttendanceSessionEntity? = attendanceDao.getSessionById(id)
    
    suspend fun insertSession(session: AttendanceSessionEntity): Long = attendanceDao.insertSession(session)
    
    suspend fun updateSession(session: AttendanceSessionEntity) = attendanceDao.updateSession(session)
    
    suspend fun deleteSession(session: AttendanceSessionEntity) = attendanceDao.deleteSession(session)
    
    // Record operations
    suspend fun getRecordsBySession(sessionId: Long): List<AttendanceRecordEntity> = 
        attendanceDao.getRecordsBySession(sessionId)
    
    suspend fun getPresentRecords(sessionId: Long): List<AttendanceRecordEntity> = 
        attendanceDao.getPresentRecords(sessionId)
    
    suspend fun getAbsentRecords(sessionId: Long): List<AttendanceRecordEntity> = 
        attendanceDao.getAbsentRecords(sessionId)
    
    suspend fun insertRecords(records: List<AttendanceRecordEntity>) = attendanceDao.insertRecords(records)
}
