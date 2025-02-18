package com.greedy.mokkoji.api.auth.dto;

public class LoginRequestDto {
    private String id;
    private String password;

    public LoginRequestDto(String id, String password) {
        this.id = id;
        this.password = password;
    }

    public String getId() {
        return id;
    }

    public String getPassword() {
        return password;
    }
}
