package com.greedy.mokkoji.api.auth.dto;

public class LoginRequestDto {
    private String studentId;
    private String password;

    public LoginRequestDto(String studentId, String password) {
        this.studentId = studentId;
        this.password = password;
    }

    public String getStudentId() {
        return studentId;
    }

    public String getPassword() {
        return password;
    }
}
