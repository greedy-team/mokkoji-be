package com.greedy.mokkoji.api.user.dto.resopnse;

import com.greedy.mokkoji.db.user.entity.User;
import com.greedy.mokkoji.enums.university.UniversityCode;
import com.greedy.mokkoji.enums.user.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;

public record UserInformationResponse(
        @Schema(example = "1") Long id,
        @Schema(example = "A1d2C3") String userCode,
        @Schema(example = "모꼬지") String name,
        @Schema(example = "user@sejong.ac.kr") String email,
        @Schema(example = "NORMAL") UserRole role,
        @Schema(example = "true") boolean emailOn,
        @Schema(example = "KONKUK") UniversityCode universityCode
) {
    public static UserInformationResponse of(final User user) {
        return new UserInformationResponse(
                user.getId(),
                user.getCode(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                user.isEmailOn(),
                user.getUniversity().getCode()
        );
    }
}
