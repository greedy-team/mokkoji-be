package com.greedy.mokkoji.api.admin.dto.response;

import com.greedy.mokkoji.enums.admin.AdminRole;
import com.greedy.mokkoji.enums.university.UniversityCode;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
public record AdminInfoResponse(
        @Schema(example = "UNIVERSITY_ADMIN") AdminRole role,
        @Schema(example = "SEJONG") UniversityCode universityCode
) {
    public static AdminInfoResponse of(final AdminRole role, final UniversityCode universityCode) {
        return AdminInfoResponse.builder()
                .role(role)
                .universityCode(universityCode)
                .build();
    }
}
