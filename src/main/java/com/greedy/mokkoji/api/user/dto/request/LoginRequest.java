package com.greedy.mokkoji.api.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "로그인 요청")
public record LoginRequest(
        @Schema(description = "학번", example = "12345678") String studentId,
        @Schema(description = "비밀번호", example = "password123") String password
) {
}

