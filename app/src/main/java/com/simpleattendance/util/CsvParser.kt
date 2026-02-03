package com.simpleattendance.util

import android.content.ContentResolver
import android.net.Uri
import java.io.BufferedReader
import java.io.InputStreamReader

data class StudentCsvRow(
    val rollNo: String,
    val name: String
)

object CsvParser {
    
    fun parseStudentsCsv(contentResolver: ContentResolver, uri: Uri): Result<List<StudentCsvRow>> {
        return try {
            val students = mutableListOf<StudentCsvRow>()
            
            contentResolver.openInputStream(uri)?.use { inputStream ->
                BufferedReader(InputStreamReader(inputStream)).use { reader ->
                    var lineNumber = 0
                    reader.forEachLine { line ->
                        lineNumber++
                        if (line.isBlank()) return@forEachLine
                        
                        val parts = line.split(",").map { it.trim() }
                        
                        when {
                            // Skip header row
                            lineNumber == 1 && isHeaderRow(parts) -> return@forEachLine
                            
                            // Two columns: RollNo, Name
                            parts.size >= 2 -> {
                                students.add(StudentCsvRow(
                                    rollNo = parts[0],
                                    name = parts[1]
                                ))
                            }
                            
                            // Single column: Name only
                            parts.size == 1 && parts[0].isNotBlank() -> {
                                students.add(StudentCsvRow(
                                    rollNo = "",
                                    name = parts[0]
                                ))
                            }
                        }
                    }
                }
            }
            
            if (students.isEmpty()) {
                Result.failure(Exception("No valid student data found in CSV"))
            } else {
                Result.success(students)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private fun isHeaderRow(parts: List<String>): Boolean {
        val headerKeywords = listOf("roll", "name", "student", "sr", "no", "number", "id")
        return parts.any { part ->
            headerKeywords.any { keyword ->
                part.lowercase().contains(keyword)
            }
        }
    }
}
