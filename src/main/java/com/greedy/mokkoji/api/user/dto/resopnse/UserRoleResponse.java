package com.greedy.mokkoji.api.user.dto.resopnse;

import io.swagger.v3.oas.annotations.media.Schema;

public record UserRoleResponse(
        @Schema(example = "CLUB_ADMIN") String role
) {
    public static UserRoleResponse of(final String role) {
        return new UserRoleResponse(role);
    }
}
