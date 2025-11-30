package com.greedy.mokkoji.api.club.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "동아리 관리 상세 정보 응답")
public record ClubManageDetailResponse(
        @Schema(example = "그리디") String name,
        @Schema(example = "ACADEMIC_CULTURAL") String category,
        @Schema(example = "DEPARTMENT_CLUB") String affiliation,
        @Schema(example = "세종대 최고의 코딩 동아리") String description,
        @Schema(example = "greedy_logo.jpg") String logo,
        @Schema(example = "https://instagram.com/greedy_sejong") String instagram
) {
    public static ClubManageDetailResponse of(final String name, final String category, final String affiliation, final String description, final String logo, final String instagram) {
        return new ClubManageDetailResponse(name, category, affiliation, description, logo, instagram);
    }
}
