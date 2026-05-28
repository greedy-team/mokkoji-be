package com.greedy.mokkoji.api.admin.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record AdminLoginRequest(
        @Schema(example = "clubmaster@konkuk.ac.kr")
        @NotBlank(message = "아이디 입력은 필수입니다.")
        String loginId,

        @Schema(example = "임시비밀번호123!")
        @NotBlank(message = "비밀번호는 필수입니다.")
        String password
) {
}
