package com.greedy.mokkoji.api.recruitment.dto.response.allRecruitment;

import com.greedy.mokkoji.enums.club.ClubAffiliation;
import com.greedy.mokkoji.enums.club.ClubCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "동아리 미리보기 응답")
public record ClubPreviewResponse(
        @Schema(description = "동아리 ID", example = "1") Long id,
        @Schema(description = "동아리명", example = "그리디") String name,
        @Schema(description = "동아리 소개", example = "세종대 최고의 코딩 동아리") String description,
        @Schema(description = "동아리 카테고리", example = "ACADEMIC_CULTURAL") ClubCategory clubCategory,
        @Schema(description = "동아리 소속", example = "DEPARTMENT_CLUB") ClubAffiliation clubAffiliation,
        @Schema(description = "동아리 로고", example = "greedy_logo.jpg") String logo
) {
}
