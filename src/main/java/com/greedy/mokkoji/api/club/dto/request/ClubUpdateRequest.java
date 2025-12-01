package com.greedy.mokkoji.api.club.dto.request;

import com.greedy.mokkoji.enums.club.ClubAffiliation;
import com.greedy.mokkoji.enums.club.ClubCategory;
import io.swagger.v3.oas.annotations.media.Schema;

public record ClubUpdateRequest(
        @Schema(example = "그리디") String name,
        @Schema(example = "ACADEMIC_CULTURAL") ClubCategory category,
        @Schema(example = "DEPARTMENT_CLUB") ClubAffiliation affiliation,
        @Schema(example = "세종대 최고의 코딩 동아리") String description,
        @Schema(example = "12345678") String clubMasterStudentId,
        @Schema(example = "greedy-logo.jpg") String logo,
        @Schema(example = "https://instagram.com/greedy_club") String instagram
) {
}
