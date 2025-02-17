package com.greedy.mokkoji.api.login.dto;

public class LoginRequestDto {
    private String id;
    private String pw;

    public LoginRequestDto(String id, String pw) {
        this.id = id;
        this.pw = pw;
    }

    public String getId() {
        return id;
    }

    public String getPw() {
        return pw;
    }
}
