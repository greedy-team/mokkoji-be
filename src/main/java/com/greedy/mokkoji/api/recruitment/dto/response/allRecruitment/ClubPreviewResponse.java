package com.greedy.mokkoji.api.recruitment.dto.response.allRecruitment;

import com.greedy.mokkoji.enums.club.ClubAffiliation;
import com.greedy.mokkoji.enums.club.ClubCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
public record ClubPreviewResponse(
        @Schema(example = "1") Long id,
        @Schema(example = "그리디") String name,
        @Schema(example = "세종대 최고의 코딩 동아리") String description,
        @Schema(example = "ACADEMIC_CULTURAL") ClubCategory clubCategory,
        @Schema(example = "DEPARTMENT_CLUB") ClubAffiliation clubAffiliation,
        @Schema(example = "https://mokkoji-app-data.s3.ap-northeast-2.amazonaws.com/club-logo/1/greedy_{UUID}.jpg") String logo
) {
}
