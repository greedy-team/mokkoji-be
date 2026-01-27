package com.greedy.mokkoji.api.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

public record LoginRequest(
        @Schema(example = "12345678") String studentId,
        @Schema(example = "password123") String password
) {
}

