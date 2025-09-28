package com.simpleattendance;

import java.util.List;

public class AttendanceReport {
    private int presentCount;
    private int absentCount;
    private List<String> absentStudents;

    public AttendanceReport() {}

    public AttendanceReport(int presentCount, int absentCount, List<String> absentStudents) {
        this.presentCount = presentCount;
        this.absentCount = absentCount;
        this.absentStudents = absentStudents;
    }

    // Getters and Setters
    public int getPresentCount() {
        return presentCount;
    }

    public void setPresentCount(int presentCount) {
        this.presentCount = presentCount;
    }

    public int getAbsentCount() {
        return absentCount;
    }

    public void setAbsentCount(int absentCount) {
        this.absentCount = absentCount;
    }

    public List<String> getAbsentStudents() {
        return absentStudents;
    }

    public void setAbsentStudents(List<String> absentStudents) {
        this.absentStudents = absentStudents;
    }

    public int getTotalCount() {
        return presentCount + absentCount;
    }

    public double getAttendancePercentage() {
        if (getTotalCount() == 0) return 0.0;
        return (double) presentCount / getTotalCount() * 100;
    }
}