package com.simpleattendance;

public class AttendanceRecord {
    private int id;
    private int sessionId;
    private int studentId;
    private String status; // P for Present, A for Absent

    public AttendanceRecord() {}

    public AttendanceRecord(int sessionId, int studentId, String status) {
        this.sessionId = sessionId;
        this.studentId = studentId;
        this.status = status;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getSessionId() {
        return sessionId;
    }

    public void setSessionId(int sessionId) {
        this.sessionId = sessionId;
    }

    public int getStudentId() {
        return studentId;
    }

    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}