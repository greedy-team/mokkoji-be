package com.greedy.mokkoji.api.club.dto.response.allClubs;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
public record ClubPreviewResponse(
        @Schema(example = "1") Long id,
        @Schema(example = "그리디") String name,
        @Schema(example = "세종대 최고의 코딩 동아리") String description,
        @Schema(example = "https://mokkoji-app-data.s3.ap-northeast-2.amazonaws.com/club-logo/1/greedy_{UUID}.jpg") String logo,
        RecruitmentPreviewResponse recruitmentPreviewResponse
) {
}
