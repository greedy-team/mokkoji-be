package com.greedy.mokkoji.api.favorite.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.YearMonth;

@Schema(description = "모집 중인 즐겨찾기 동아리 조회 요청")
public record RecruitClubsRequest(
        @Schema(description = "조회할 년월", example = "2025-11") YearMonth yearMonth
) {
}
