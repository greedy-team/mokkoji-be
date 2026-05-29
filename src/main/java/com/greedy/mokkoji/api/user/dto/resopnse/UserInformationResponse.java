package com.greedy.mokkoji.api.user.dto.resopnse;

import com.greedy.mokkoji.db.university.entity.University;
import com.greedy.mokkoji.db.user.entity.User;
import com.greedy.mokkoji.enums.university.UniversityCode;
import com.greedy.mokkoji.enums.user.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;

public record UserInformationResponse(
        @Schema(example = "1") Long id,
        @Schema(example = "모꼬지") String name,
        @Schema(example = "user@sejong.ac.kr") String email,
        @Schema(example = "NORMAL") UserRole role,
        @Schema(example = "true") boolean emailOn,
        @Schema(example = "KONKUK", description = "학교 미선택 시 null") UniversityCode universityCode
) {
    public static UserInformationResponse of(final User user) {
        final University university = user.getUniversity();
        final UniversityCode universityCode = university == null ? null : university.getCode();

        return new UserInformationResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                user.isEmailOn(),
                universityCode
        );
    }
}
