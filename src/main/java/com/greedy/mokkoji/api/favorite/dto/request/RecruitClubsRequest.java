package com.greedy.mokkoji.api.favorite.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.YearMonth;

public record RecruitClubsRequest(
        @Schema(example = "2025-11") YearMonth yearMonth
) {
}
