package com.greedy.mokkoji.api.club.dto.request;

import com.greedy.mokkoji.enums.club.ClubAffiliation;
import com.greedy.mokkoji.enums.club.ClubCategory;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "동아리 정보 수정 요청")
public record ClubUpdateRequest(
        @Schema(description = "동아리명", example = "그리디") String name,
        @Schema(description = "동아리 카테고리", example = "ACADEMIC_CULTURAL") ClubCategory category,
        @Schema(description = "동아리 소속", example = "DEPARTMENT_CLUB") ClubAffiliation affiliation,
        @Schema(description = "동아리 소개", example = "세종대 최고의 코딩 동아리") String description,
        @Schema(description = "동아리장 학번", example = "12345678") String clubMasterStudentId,
        @Schema(description = "동아리 로고 이미지 URL", example = "greedy-logo.jpg") String logo,
        @Schema(description = "동아리 인스타그램 URL", example = "https://instagram.com/greedy_club") String instagram
) {
}
