package com.greedy.mokkoji.api.user.dto.resopnse;

import java.util.List;

public record UserManageClubsResponse (
        List<UserManageClubResponse> clubs
) {
    public static UserManageClubsResponse of(final List<UserManageClubResponse> clubs) {
        return new UserManageClubsResponse(clubs);
    }
}
