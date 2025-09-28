package com.simpleattendance;

public class Student {
    private int id;
    private int classId;
    private String rollNo;
    private String name;

    public Student() {}

    public Student(int classId, String rollNo, String name) {
        this.classId = classId;
        this.rollNo = rollNo;
        this.name = name;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getClassId() {
        return classId;
    }

    public void setClassId(int classId) {
        this.classId = classId;
    }

    public String getRollNo() {
        return rollNo;
    }

    public void setRollNo(String rollNo) {
        this.rollNo = rollNo;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDisplayName() {
        return rollNo != null && !rollNo.isEmpty() ? rollNo + " - " + name : name;
    }

    @Override
    public String toString() {
        return getDisplayName();
    }
}