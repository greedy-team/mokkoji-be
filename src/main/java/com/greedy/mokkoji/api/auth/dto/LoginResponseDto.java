package com.greedy.mokkoji.api.auth.dto;

import java.util.HashMap;
import java.util.Map;

public class LoginResponseDto {
    private Map<String, String> data;

    public LoginResponseDto (String accessToken, String refreshToken) {
        this.data = new HashMap<>();
        this.data.put("accessToken", accessToken);
        this.data.put("refreshToken", refreshToken);
    }

    public Map<String, String> getData() {
        return data;
    }

    public void setData(Map<String, String> data) {
        this.data = data;
    }
}
