package com.greedy.mokkoji.api.club.dto.request;

import com.greedy.mokkoji.enums.club.ClubAffiliation;
import com.greedy.mokkoji.enums.club.ClubCategory;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "동아리 생성 요청")
public record ClubCreateRequest(
        @Schema(description = "동아리명", example = "그리디") String name,
        @Schema(description = "동아리 카테고리", example = "ACADEMIC_CULTURAL") ClubCategory category,
        @Schema(description = "동아리 소속", example = "DEPARTMENT_CLUB") ClubAffiliation affiliation,
        @Schema(description = "동아리장 학번", example = "12345678") String clubMasterStudentId
) {
}
