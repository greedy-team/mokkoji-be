package com.greedy.mokkoji.api.user.dto.request;

public record LoginRequest(
        String studentId,
        String password
) {
}
