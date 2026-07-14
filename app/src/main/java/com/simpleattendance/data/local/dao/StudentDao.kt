package com.simpleattendance.data.local.dao

import androidx.room.*
import com.simpleattendance.data.local.entity.StudentEntity
import kotlinx.coroutines.flow.Flow

/**
 * Room DAO for student operations.
 * Declared as an abstract class (not interface) so that the concrete
 * @Transaction updateRoster() method is supported by Room/KSP.
 */
@Dao
abstract class StudentDao {

    @Query("SELECT * FROM students WHERE classId = :classId ORDER BY rollNo, name")
    abstract fun getStudentsByClass(classId: Long): Flow<List<StudentEntity>>

    @Query("SELECT * FROM students WHERE classId = :classId ORDER BY rollNo, name")
    abstract suspend fun getStudentsByClassSync(classId: Long): List<StudentEntity>

    @Query("SELECT * FROM students WHERE id = :id")
    abstract suspend fun getStudentById(id: Long): StudentEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertStudent(student: StudentEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertStudents(students: List<StudentEntity>)

    @Update
    abstract suspend fun updateStudent(student: StudentEntity)

    @Delete
    abstract suspend fun deleteStudent(student: StudentEntity)

    @Query("DELETE FROM students WHERE classId = :classId")
    abstract suspend fun deleteStudentsByClass(classId: Long)

    /**
     * Atomically replaces the full student roster for a class inside one Room transaction.
     * A crash mid-operation cannot leave the roster half-written.
     * Historical attendance records are safe — cascade delete is on class delete, not here.
     */
    @Transaction
    open suspend fun updateRoster(classId: Long, students: List<StudentEntity>) {
        deleteStudentsByClass(classId)
        insertStudents(students)
    }
}
