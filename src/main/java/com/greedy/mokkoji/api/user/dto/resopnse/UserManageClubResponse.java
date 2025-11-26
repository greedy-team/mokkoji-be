package com.greedy.mokkoji.api.user.dto.resopnse;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "사용자가 관리하는 동아리 응답")
public record UserManageClubResponse(
        @Schema(description = "동아리 ID", example = "1") Long clubId,
        @Schema(description = "동아리명", example = "그리디") String clubName
) {
}
