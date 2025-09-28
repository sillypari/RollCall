package com.simpleattendance;

import android.content.Context;
import android.net.Uri;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class CSVHelper {
    
    public static List<Student> parseCSVFile(Uri fileUri, Context context, int classId) {
        List<Student> students = new ArrayList<>();
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(fileUri);
            if (inputStream == null) {
                return students;
            }
            
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            boolean isFirstLine = true;
            
            while ((line = reader.readLine()) != null) {
                if (isFirstLine) {
                    isFirstLine = false;
                    // Skip header line if it exists
                    if (line.toLowerCase().contains("roll") || line.toLowerCase().contains("name")) {
                        continue;
                    }
                }
                
                String[] values = line.split(",");
                if (values.length >= 1) {
                    Student student = new Student();
                    student.setClassId(classId);
                    
                    if (values.length == 1) {
                        // Only name provided
                        student.setName(values[0].trim());
                        student.setRollNo("");
                    } else {
                        // Both roll number and name provided
                        student.setRollNo(values[0].trim());
                        student.setName(values[1].trim());
                    }
                    
                    if (!student.getName().isEmpty()) {
                        students.add(student);
                    }
                }
            }
            
            reader.close();
            inputStream.close();
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return students;
    }

    public static boolean isValidCSVFormat(Uri fileUri, Context context) {
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(fileUri);
            if (inputStream == null) {
                return false;
            }
            
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String firstLine = reader.readLine();
            reader.close();
            inputStream.close();
            
            if (firstLine == null || firstLine.trim().isEmpty()) {
                return false;
            }
            
            String[] values = firstLine.split(",");
            return values.length >= 1;
            
        } catch (Exception e) {
            return false;
        }
    }

    public static String getCSVFormatExample() {
        return "CSV Format Examples:\n\n" +
               "Option 1 (Roll No, Name):\n" +
               "101,John Doe\n" +
               "102,Jane Smith\n" +
               "103,Bob Johnson\n\n" +
               "Option 2 (Name only):\n" +
               "John Doe\n" +
               "Jane Smith\n" +
               "Bob Johnson\n\n" +
               "Note: First line can be a header (will be skipped automatically)";
    }
}