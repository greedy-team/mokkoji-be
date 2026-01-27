package com.greedy.mokkoji.api.club.dto.response;

import com.greedy.mokkoji.enums.club.ClubAffiliation;
import com.greedy.mokkoji.enums.club.ClubCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
public record ClubManageDetailResponse(
        @Schema(example = "그리디") String name,
        @Schema(example = "ACADEMIC_CULTURAL") ClubCategory category,
        @Schema(example = "DEPARTMENT_CLUB") ClubAffiliation affiliation,
        @Schema(example = "세종대 최고의 코딩 동아리") String description,
        @Schema(example = "https://mokkoji-app-data.s3.ap-northeast-2.amazonaws.com/club-logo/1/greedy_{UUID}.jpg") String logo,
        @Schema(example = "https://instagram.com/greedy_sejong") String instagram
) {
    public static ClubManageDetailResponse of(
            final String name,
            final ClubCategory category,
            final ClubAffiliation affiliation,
            final String description,
            final String logo,
            final String instagram
    ) {
        return new ClubManageDetailResponse(name, category, affiliation, description, logo, instagram);
    }
}
