package com.greedy.mokkoji.api.admin.dto.response;

import com.greedy.mokkoji.enums.admin.AdminRole;
import com.greedy.mokkoji.enums.university.UniversityCode;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
public record AdminLoginResponse(
        @Schema(example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...") String accessToken,
        @Schema(example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...") String refreshToken,
        @Schema(example = "UNIVERSITY_ADMIN") AdminRole role,
        @Schema(example = "SEJONG") UniversityCode universityCode
) {
    public static AdminLoginResponse of(
            final String accessToken,
            final String refreshToken,
            final AdminRole role,
            final UniversityCode universityCode
    ) {
        return AdminLoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .role(role)
                .universityCode(universityCode)
                .build();
    }
}
