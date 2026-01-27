package com.greedy.mokkoji.api.user.dto.resopnse;

import com.greedy.mokkoji.enums.user.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;

public record UserRoleResponse(
        @Schema(example = "CLUB_ADMIN") UserRole role
) {
    public static UserRoleResponse of(final UserRole role) {
        return new UserRoleResponse(role);
    }
}
