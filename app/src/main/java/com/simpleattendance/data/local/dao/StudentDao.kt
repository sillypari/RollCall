package com.simpleattendance.data.local.dao

import androidx.room.*
import com.simpleattendance.data.local.entity.StudentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StudentDao {
    @Query("SELECT * FROM students WHERE classId = :classId ORDER BY rollNo, name")
    fun getStudentsByClass(classId: Long): Flow<List<StudentEntity>>
    
    @Query("SELECT * FROM students WHERE classId = :classId ORDER BY rollNo, name")
    suspend fun getStudentsByClassSync(classId: Long): List<StudentEntity>
    
    @Query("SELECT * FROM students WHERE id = :id")
    suspend fun getStudentById(id: Long): StudentEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudent(student: StudentEntity): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudents(students: List<StudentEntity>)
    
    @Update
    suspend fun updateStudent(student: StudentEntity)
    
    @Delete
    suspend fun deleteStudent(student: StudentEntity)
    
    @Query("DELETE FROM students WHERE classId = :classId")
    suspend fun deleteStudentsByClass(classId: Long)
}
