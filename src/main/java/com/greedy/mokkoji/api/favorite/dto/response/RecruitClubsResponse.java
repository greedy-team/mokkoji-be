package com.greedy.mokkoji.api.favorite.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "즐겨찾기 동아리 모집 정보 응답")
public record RecruitClubsResponse(
        @Schema(description = "동아리 ID", example = "1") Long clubId,
        @Schema(description = "동아리명", example = "그리디") String clubName,
        @Schema(description = "모집 시작일", example = "2025-11-25T00:00:00") LocalDateTime recruitStart,
        @Schema(description = "모집 종료일", example = "2025-12-04T23:59:59") LocalDateTime recruitEnd
) {
    public static RecruitClubsResponse of(Long clubId, String clubName, LocalDateTime recruitStart, LocalDateTime recruitEnd) {
        return new RecruitClubsResponse(clubId, clubName, recruitStart, recruitEnd);
    }
}
