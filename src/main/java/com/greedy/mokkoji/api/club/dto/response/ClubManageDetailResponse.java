package com.greedy.mokkoji.api.club.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "동아리 관리 상세 정보 응답")
public record ClubManageDetailResponse(
        @Schema(description = "동아리명", example = "그리디") String name,
        @Schema(description = "동아리 카테고리", example = "학술/교양") String category,
        @Schema(description = "동아리 소속", example = "정인준/가인준") String affiliation,
        @Schema(description = "동아리 소개", example = "세종대 최고의 코딩 동아리") String description,
        @Schema(description = "동아리 로고", example = "greedy_logo.jpg") String logo,
        @Schema(description = "인스타그램 URL", example = "https://instagram.com/greedy_sejong") String instagram
) {
    public static ClubManageDetailResponse of(final String name, final String category, final String affiliation, final String description, final String logo, final String instagram) {
        return new ClubManageDetailResponse(name, category, affiliation, description, logo, instagram);
    }
}
