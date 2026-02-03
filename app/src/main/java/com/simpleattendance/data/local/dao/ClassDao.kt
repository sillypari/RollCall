package com.simpleattendance.data.local.dao

import androidx.room.*
import com.simpleattendance.data.local.entity.ClassEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ClassDao {
    @Query("SELECT * FROM classes ORDER BY createdAt DESC")
    fun getAllClasses(): Flow<List<ClassEntity>>
    
    @Query("SELECT * FROM classes WHERE id = :id")
    suspend fun getClassById(id: Long): ClassEntity?
    
    @Query("SELECT * FROM classes WHERE branch = :branch AND semester = :semester AND section = :section")
    suspend fun getClassesByBatch(branch: String, semester: String, section: String): List<ClassEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClass(classEntity: ClassEntity): Long
    
    @Update
    suspend fun updateClass(classEntity: ClassEntity)
    
    @Delete
    suspend fun deleteClass(classEntity: ClassEntity)
    
    @Query("DELETE FROM classes WHERE id = :id")
    suspend fun deleteClassById(id: Long)
}
