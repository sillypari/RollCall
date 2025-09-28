package com.simpleattendance;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "attendance.db";
    private static final int DATABASE_VERSION = 2;

    // Tables
    private static final String TABLE_CLASSES = "classes";
    private static final String TABLE_STUDENTS = "students";
    private static final String TABLE_SUBJECTS = "subjects";
    private static final String TABLE_ATTENDANCE_SESSIONS = "attendance_sessions";
    private static final String TABLE_ATTENDANCE_RECORDS = "attendance_records";

    // Common columns
    private static final String KEY_ID = "id";

    // Classes table columns
    private static final String KEY_BRANCH = "branch";
    private static final String KEY_SEMESTER = "semester";
    private static final String KEY_SECTION = "section";
    private static final String KEY_SUBJECT = "subject";
    private static final String KEY_CREATED_DATE = "created_date";

    // Students table columns
    private static final String KEY_CLASS_ID = "class_id";
    private static final String KEY_ROLL_NO = "roll_no";
    private static final String KEY_NAME = "name";

    // Subjects table columns
    private static final String KEY_SUBJECT_NAME = "name";

    // Attendance sessions table columns
    private static final String KEY_SUBJECT_ID = "subject_id";
    private static final String KEY_DATE = "date";
    private static final String KEY_TIME = "time";
    private static final String KEY_NOTES = "notes";

    // Attendance records table columns
    private static final String KEY_SESSION_ID = "session_id";
    private static final String KEY_STUDENT_ID = "student_id";
    private static final String KEY_STATUS = "status";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create classes table
        String CREATE_CLASSES_TABLE = "CREATE TABLE " + TABLE_CLASSES + "("
                + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + KEY_BRANCH + " TEXT NOT NULL,"
                + KEY_SEMESTER + " TEXT NOT NULL,"
                + KEY_SECTION + " TEXT NOT NULL,"
                + KEY_SUBJECT + " TEXT DEFAULT '',"
                + KEY_CREATED_DATE + " TEXT,"
                + "UNIQUE(" + KEY_BRANCH + "," + KEY_SEMESTER + "," + KEY_SECTION + ")"
                + ")";
        db.execSQL(CREATE_CLASSES_TABLE);

        // Create students table
        String CREATE_STUDENTS_TABLE = "CREATE TABLE " + TABLE_STUDENTS + "("
                + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + KEY_CLASS_ID + " INTEGER,"
                + KEY_ROLL_NO + " TEXT,"
                + KEY_NAME + " TEXT NOT NULL,"
                + "FOREIGN KEY(" + KEY_CLASS_ID + ") REFERENCES " + TABLE_CLASSES + "(" + KEY_ID + ")"
                + ")";
        db.execSQL(CREATE_STUDENTS_TABLE);

        // Create subjects table
        String CREATE_SUBJECTS_TABLE = "CREATE TABLE " + TABLE_SUBJECTS + "("
                + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + KEY_SUBJECT_NAME + " TEXT NOT NULL"
                + ")";
        db.execSQL(CREATE_SUBJECTS_TABLE);

        // Create attendance sessions table
        String CREATE_ATTENDANCE_SESSIONS_TABLE = "CREATE TABLE " + TABLE_ATTENDANCE_SESSIONS + "("
                + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + KEY_CLASS_ID + " INTEGER,"
                + KEY_SUBJECT_ID + " INTEGER,"
                + KEY_DATE + " TEXT,"
                + KEY_TIME + " TEXT,"
                + KEY_NOTES + " TEXT,"
                + "FOREIGN KEY(" + KEY_CLASS_ID + ") REFERENCES " + TABLE_CLASSES + "(" + KEY_ID + "),"
                + "FOREIGN KEY(" + KEY_SUBJECT_ID + ") REFERENCES " + TABLE_SUBJECTS + "(" + KEY_ID + ")"
                + ")";
        db.execSQL(CREATE_ATTENDANCE_SESSIONS_TABLE);

        // Create attendance records table
        String CREATE_ATTENDANCE_RECORDS_TABLE = "CREATE TABLE " + TABLE_ATTENDANCE_RECORDS + "("
                + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + KEY_SESSION_ID + " INTEGER,"
                + KEY_STUDENT_ID + " INTEGER,"
                + KEY_STATUS + " TEXT CHECK(" + KEY_STATUS + " IN ('P', 'A')),"
                + "FOREIGN KEY(" + KEY_SESSION_ID + ") REFERENCES " + TABLE_ATTENDANCE_SESSIONS + "(" + KEY_ID + "),"
                + "FOREIGN KEY(" + KEY_STUDENT_ID + ") REFERENCES " + TABLE_STUDENTS + "(" + KEY_ID + ")"
                + ")";
        db.execSQL(CREATE_ATTENDANCE_RECORDS_TABLE);

        // Insert default subjects
        insertDefaultSubjects(db);
    }

    private void insertDefaultSubjects(SQLiteDatabase db) {
        String[] subjects = {"Mathematics", "Physics", "Chemistry", "English", "Computer Science", 
                           "Biology", "History", "Geography", "Economics", "Political Science"};
        
        for (String subject : subjects) {
            ContentValues values = new ContentValues();
            values.put(KEY_SUBJECT_NAME, subject);
            db.insert(TABLE_SUBJECTS, null, values);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            // Add subject column to classes table
            db.execSQL("ALTER TABLE " + TABLE_CLASSES + " ADD COLUMN " + KEY_SUBJECT + " TEXT DEFAULT ''");
        }
    }

    // Class operations
    public long insertClass(ClassModel classModel) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_BRANCH, classModel.getBranch());
        values.put(KEY_SEMESTER, classModel.getSemester());
        values.put(KEY_SECTION, classModel.getSection());
        values.put(KEY_SUBJECT, classModel.getSubject());
        values.put(KEY_CREATED_DATE, classModel.getCreatedDate());
        
        long result = db.insert(TABLE_CLASSES, null, values);
        db.close();
        return result;
    }

    public List<ClassModel> getAllClasses() {
        List<ClassModel> classList = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_CLASSES + " ORDER BY " + KEY_CREATED_DATE + " DESC";
        
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        
        if (cursor.moveToFirst()) {
            do {
                ClassModel classModel = new ClassModel();
                classModel.setId(cursor.getInt(cursor.getColumnIndexOrThrow(KEY_ID)));
                classModel.setBranch(cursor.getString(cursor.getColumnIndexOrThrow(KEY_BRANCH)));
                classModel.setSemester(cursor.getString(cursor.getColumnIndexOrThrow(KEY_SEMESTER)));
                classModel.setSection(cursor.getString(cursor.getColumnIndexOrThrow(KEY_SECTION)));
                
                // Handle subject field with backwards compatibility
                int subjectColumnIndex = cursor.getColumnIndex(KEY_SUBJECT);
                if (subjectColumnIndex != -1) {
                    classModel.setSubject(cursor.getString(subjectColumnIndex));
                } else {
                    classModel.setSubject("");
                }
                
                classModel.setCreatedDate(cursor.getString(cursor.getColumnIndexOrThrow(KEY_CREATED_DATE)));
                classList.add(classModel);
            } while (cursor.moveToNext());
        }
        
        cursor.close();
        db.close();
        return classList;
    }

    // Student operations
    public long insertStudent(Student student) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_CLASS_ID, student.getClassId());
        values.put(KEY_ROLL_NO, student.getRollNo());
        values.put(KEY_NAME, student.getName());
        
        long result = db.insert(TABLE_STUDENTS, null, values);
        db.close();
        return result;
    }

    public List<Student> getStudentsByClass(int classId) {
        List<Student> studentList = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_STUDENTS + " WHERE " + KEY_CLASS_ID + " = ?";
        
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, new String[]{String.valueOf(classId)});
        
        if (cursor.moveToFirst()) {
            do {
                Student student = new Student();
                student.setId(cursor.getInt(cursor.getColumnIndexOrThrow(KEY_ID)));
                student.setClassId(cursor.getInt(cursor.getColumnIndexOrThrow(KEY_CLASS_ID)));
                student.setRollNo(cursor.getString(cursor.getColumnIndexOrThrow(KEY_ROLL_NO)));
                student.setName(cursor.getString(cursor.getColumnIndexOrThrow(KEY_NAME)));
                studentList.add(student);
            } while (cursor.moveToNext());
        }
        
        cursor.close();
        db.close();
        return studentList;
    }

    public Cursor getStudentsCursorByClass(int classId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String selectQuery = "SELECT * FROM " + TABLE_STUDENTS + " WHERE " + KEY_CLASS_ID + " = ?";
        return db.rawQuery(selectQuery, new String[]{String.valueOf(classId)});
    }

    // Subject operations
    public List<Subject> getAllSubjects() {
        List<Subject> subjectList = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_SUBJECTS + " ORDER BY " + KEY_SUBJECT_NAME;
        
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        
        if (cursor.moveToFirst()) {
            do {
                Subject subject = new Subject();
                subject.setId(cursor.getInt(cursor.getColumnIndexOrThrow(KEY_ID)));
                subject.setName(cursor.getString(cursor.getColumnIndexOrThrow(KEY_SUBJECT_NAME)));
                subjectList.add(subject);
            } while (cursor.moveToNext());
        }
        
        cursor.close();
        db.close();
        return subjectList;
    }

    // Attendance session operations
    public long insertAttendanceSession(AttendanceSession session) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_CLASS_ID, session.getClassId());
        values.put(KEY_SUBJECT_ID, session.getSubjectId());
        values.put(KEY_DATE, session.getDate());
        values.put(KEY_TIME, session.getTime());
        values.put(KEY_NOTES, session.getNotes());
        
        long result = db.insert(TABLE_ATTENDANCE_SESSIONS, null, values);
        db.close();
        return result;
    }

    // Attendance record operations
    public long insertAttendanceRecord(AttendanceRecord record) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_SESSION_ID, record.getSessionId());
        values.put(KEY_STUDENT_ID, record.getStudentId());
        values.put(KEY_STATUS, record.getStatus());
        
        long result = db.insert(TABLE_ATTENDANCE_RECORDS, null, values);
        db.close();
        return result;
    }

    public AttendanceReport generateReport(int sessionId) {
        int presentCount = 0;
        int absentCount = 0;
        List<String> absentStudents = new ArrayList<>();
        
        String query = "SELECT s." + KEY_NAME + ", ar." + KEY_STATUS + 
                      " FROM " + TABLE_ATTENDANCE_RECORDS + " ar " +
                      " INNER JOIN " + TABLE_STUDENTS + " s ON ar." + KEY_STUDENT_ID + " = s." + KEY_ID +
                      " WHERE ar." + KEY_SESSION_ID + " = ?";
        
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(sessionId)});
        
        if (cursor.moveToFirst()) {
            do {
                String status = cursor.getString(cursor.getColumnIndexOrThrow(KEY_STATUS));
                if ("P".equals(status)) {
                    presentCount++;
                } else {
                    absentCount++;
                    String name = cursor.getString(cursor.getColumnIndexOrThrow(KEY_NAME));
                    absentStudents.add(name);
                }
            } while (cursor.moveToNext());
        }
        
        cursor.close();
        db.close();
        
        return new AttendanceReport(presentCount, absentCount, absentStudents);
    }

    public ClassModel getClassById(int classId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_CLASSES + " WHERE " + KEY_ID + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(classId)});
        
        ClassModel classModel = null;
        if (cursor.moveToFirst()) {
            classModel = new ClassModel();
            classModel.setId(cursor.getInt(cursor.getColumnIndexOrThrow(KEY_ID)));
            classModel.setBranch(cursor.getString(cursor.getColumnIndexOrThrow(KEY_BRANCH)));
            classModel.setSemester(cursor.getString(cursor.getColumnIndexOrThrow(KEY_SEMESTER)));
            classModel.setSection(cursor.getString(cursor.getColumnIndexOrThrow(KEY_SECTION)));
            classModel.setCreatedDate(cursor.getString(cursor.getColumnIndexOrThrow(KEY_CREATED_DATE)));
        }
        
        cursor.close();
        db.close();
        return classModel;
    }

    public Subject getSubjectById(int subjectId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_SUBJECTS + " WHERE " + KEY_ID + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(subjectId)});
        
        Subject subject = null;
        if (cursor.moveToFirst()) {
            subject = new Subject();
            subject.setId(cursor.getInt(cursor.getColumnIndexOrThrow(KEY_ID)));
            subject.setName(cursor.getString(cursor.getColumnIndexOrThrow(KEY_SUBJECT_NAME)));
        }
        
        cursor.close();
        db.close();
        return subject;
    }
}