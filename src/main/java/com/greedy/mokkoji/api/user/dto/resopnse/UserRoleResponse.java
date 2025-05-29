package com.greedy.mokkoji.api.user.dto.resopnse;

public record UserRoleResponse(
        String role
) {
    public static UserRoleResponse of(final String role) {
        return new UserRoleResponse(role);
    }
}
