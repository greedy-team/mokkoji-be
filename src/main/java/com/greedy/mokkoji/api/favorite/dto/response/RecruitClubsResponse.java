package com.greedy.mokkoji.api.favorite.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

public record RecruitClubsResponse(
        @Schema(example = "1") Long clubId,
        @Schema(example = "그리디") String clubName,
        @Schema(example = "2025-11-25T00:00:00") LocalDateTime recruitStart,
        @Schema(example = "2025-12-04T23:59:59") LocalDateTime recruitEnd
) {
    public static RecruitClubsResponse of(Long clubId, String clubName, LocalDateTime recruitStart, LocalDateTime recruitEnd) {
        return new RecruitClubsResponse(clubId, clubName, recruitStart, recruitEnd);
    }
}
