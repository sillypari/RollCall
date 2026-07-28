package com.simpleattendance.data.local.dao

import androidx.room.*
import com.simpleattendance.data.local.entity.StudentEntity
import kotlinx.coroutines.flow.Flow

data class ClassStudentCount(
    val classId: Long,
    val studentCount: Int
)

/**
 * Room DAO for student operations.
 * Declared as an abstract class (not interface) so that the concrete
 * @Transaction updateRoster() method is supported by Room/KSP.
 */
@Dao
abstract class StudentDao {

    @Query("SELECT * FROM students WHERE classId = :classId AND isActive = 1 ORDER BY rollNo, name")
    abstract fun getStudentsByClass(classId: Long): Flow<List<StudentEntity>>

    @Query("SELECT * FROM students WHERE classId = :classId AND isActive = 1 ORDER BY rollNo, name")
    abstract suspend fun getStudentsByClassSync(classId: Long): List<StudentEntity>

    @Query("SELECT classId, COUNT(*) AS studentCount FROM students WHERE isActive = 1 GROUP BY classId")
    abstract fun getStudentCounts(): Flow<List<ClassStudentCount>>

    @Query("SELECT * FROM students WHERE classId = :classId ORDER BY id")
    abstract suspend fun getAllStudentsByClassSync(classId: Long): List<StudentEntity>

    @Query("SELECT * FROM students WHERE id = :id")
    abstract suspend fun getStudentById(id: Long): StudentEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertStudent(student: StudentEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertStudents(students: List<StudentEntity>)

    @Update
    abstract suspend fun updateStudent(student: StudentEntity)

    @Update
    abstract suspend fun updateStudents(students: List<StudentEntity>)

    @Delete
    abstract suspend fun deleteStudent(student: StudentEntity)

    @Query("DELETE FROM students WHERE classId = :classId")
    abstract suspend fun deleteStudentsByClass(classId: Long)

    /** Updates the active roster without deleting students referenced by old sessions. */
    @Transaction
    open suspend fun updateRoster(classId: Long, students: List<StudentEntity>) {
        val unmatchedExisting = getAllStudentsByClassSync(classId).toMutableList()
        val existingUpdates = mutableListOf<StudentEntity>()
        val newStudents = mutableListOf<StudentEntity>()

        students.forEach { incoming ->
            val normalizedRoll = incoming.rollNo.trim()
            val normalizedName = incoming.name.trim()
            val match = unmatchedExisting.firstOrNull { existing ->
                normalizedRoll.isNotEmpty() &&
                    existing.rollNo.equals(normalizedRoll, ignoreCase = true)
            } ?: unmatchedExisting.firstOrNull { existing ->
                existing.name.equals(normalizedName, ignoreCase = true)
            }

            if (match == null) {
                newStudents += incoming.copy(
                    id = 0,
                    classId = classId,
                    rollNo = normalizedRoll,
                    name = normalizedName,
                    isActive = true
                )
            } else {
                unmatchedExisting.remove(match)
                existingUpdates += match.copy(
                    rollNo = normalizedRoll,
                    name = normalizedName,
                    isActive = true
                )
            }
        }

        existingUpdates += unmatchedExisting.map { it.copy(isActive = false) }
        if (existingUpdates.isNotEmpty()) updateStudents(existingUpdates)
        if (newStudents.isNotEmpty()) insertStudents(newStudents)
    }
}
