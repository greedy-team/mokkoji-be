package com.greedy.mokkoji.api.login.dto;

public class StudentInformationResponseDto {
    private String name;
    private String studentId;
    private String department;
    private String grade;

    public StudentInformationResponseDto(String name, String studentId, String department, String grade) {
        this.name = name;
        this.studentId = studentId;
        this.department = department;
        this.grade = grade;
    }

    public String getName() {
        return name;
    }

    public String getStudentId() {
        return studentId;
    }

    public String getDepartment() {
        return department;
    }

    public String getGrade() {
        return grade;
    }
}
