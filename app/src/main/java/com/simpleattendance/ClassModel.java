package com.simpleattendance;

public class ClassModel {
    private int id;
    private String branch;
    private String semester;
    private String section;
    private String createdDate;

    public ClassModel() {}

    public ClassModel(String branch, String semester, String section, String createdDate) {
        this.branch = branch;
        this.semester = semester;
        this.section = section;
        this.createdDate = createdDate;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

    public String getSemester() {
        return semester;
    }

    public void setSemester(String semester) {
        this.semester = semester;
    }

    public String getSection() {
        return section;
    }

    public void setSection(String section) {
        this.section = section;
    }

    public String getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(String createdDate) {
        this.createdDate = createdDate;
    }

    public String getDisplayName() {
        return branch + " - " + semester + " - " + section;
    }

    @Override
    public String toString() {
        return getDisplayName();
    }
}