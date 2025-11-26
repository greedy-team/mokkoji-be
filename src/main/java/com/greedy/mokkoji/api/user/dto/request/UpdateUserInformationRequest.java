package com.greedy.mokkoji.api.user.dto.request;

import jakarta.validation.constraints.Pattern;

public record UpdateUserInformationRequest(
    @Pattern(
        regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$",
        message = "유효하지 않은 이메일 형식입니다."
    )
    String email
) {

}
