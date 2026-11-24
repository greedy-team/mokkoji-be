package com.greedy.mokkoji.api.user.dto.resopnse;

import com.greedy.mokkoji.enums.university.UniversityCode;
import com.greedy.mokkoji.enums.user.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
public record LoginResponse(
        @Schema(example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...") String accessToken,
        @Schema(example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...") String refreshToken,
        @Schema(example = "false") Boolean isNewUser,
        @Schema(example = "NORMAL") UserRole role,
        @Schema(example = "SEJONG") UniversityCode universityCode
) {
    public static LoginResponse of(
            final String accessToken,
            final String refreshToken,
            final Boolean isNewUser,
            final UserRole role,
            final UniversityCode universityCode
    ) {
        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .isNewUser(isNewUser)
                .role(role)
                .universityCode(universityCode)
                .build();
    }
}
