package com.greedy.mokkoji.api.user.dto.resopnse;

import io.swagger.v3.oas.annotations.media.Schema;

public record UserManageClubResponse(
        @Schema(example = "1") Long clubId,
        @Schema(example = "그리디") String clubName
) {
}
