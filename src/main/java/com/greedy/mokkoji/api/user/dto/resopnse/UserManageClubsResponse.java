package com.greedy.mokkoji.api.user.dto.resopnse;

import java.util.List;

public record UserManageClubsResponse (
        List<Long> clubIds
) {
    public static UserManageClubsResponse of(final List<Long> clubIds) {
        return new UserManageClubsResponse(clubIds);
    }
}
