package com.greedy.mokkoji.api.user.dto.resopnse;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "사용자가 관리하는 동아리 목록 응답")
public record UserManageClubsResponse(
        @Schema(description = "관리 중인 동아리 목록") List<UserManageClubResponse> clubs
) {
    public static UserManageClubsResponse of(final List<UserManageClubResponse> clubs) {
        return new UserManageClubsResponse(clubs);
    }
}
