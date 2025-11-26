package com.greedy.mokkoji.api.user.dto.resopnse;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "사용자 권한 응답")
public record UserRoleResponse(
        @Schema(description = "사용자 권한", example = "CLUB_ADMIN") String role
) {
    public static UserRoleResponse of(final String role) {
        return new UserRoleResponse(role);
    }
}
