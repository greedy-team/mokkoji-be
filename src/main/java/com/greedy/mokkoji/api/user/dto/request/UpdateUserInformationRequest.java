package com.greedy.mokkoji.api.user.dto.request;

import com.greedy.mokkoji.enums.university.UniversityCode;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;

public record UpdateUserInformationRequest(
        @Schema(example = "모꼬지") String name,
        @Schema(example = "user@sejong.ac.kr")
        @Pattern(
                regexp = "(^$)|(^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$)",
                message = "유효하지 않은 이메일 형식입니다."
        )
        String email,
        @Schema(example = "false") Boolean isEmailOn,
        @Schema(example = "SEJONG", description = "학교 코드") UniversityCode universityCode
) {

}
