package com.greedy.mokkoji.api.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.regex.Pattern;

public record UpdateUserInformationRequest(
        @Schema(example = "user@sejong.ac.kr") String email) {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"
    );

    public UpdateUserInformationRequest(String email) {
        validateEmail(email);
        this.email = email;
    }

    private void validateEmail(String email) {
        if (email == null || !EMAIL_PATTERN.matcher(email).matches()) {
            throw new IllegalArgumentException("유효하지 않은 이메일 형식입니다: " + email);
        }
    }
}
