package com.greedy.mokkoji.api.auth.dto;

public class StudentInformationResponseDto {
    private String name;
    private String department;
    private String grade;

    public StudentInformationResponseDto(String name, String department, String grade) {
        this.name = name;
        this.department = department;
        this.grade = grade;
    }

    public String getName() {
        return name;
    }

    public String getDepartment() {
        return department;
    }

    public String getGrade() {
        return grade;
    }
}
