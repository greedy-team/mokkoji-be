package com.greedy.mokkoji.api.club.dto.request;

import com.greedy.mokkoji.enums.club.ClubAffiliation;
import com.greedy.mokkoji.enums.club.ClubCategory;
import com.greedy.mokkoji.enums.university.UniversityCode;
import io.swagger.v3.oas.annotations.media.Schema;

public record ClubCreateRequest(
        @Schema(example = "그리디") String name,
        @Schema(example = "ACADEMIC_CULTURAL") ClubCategory category,
        @Schema(example = "DEPARTMENT_CLUB") ClubAffiliation affiliation,
        @Schema(example = "12345678") String clubMasterStudentId,
        @Schema(example = "SEJONG") UniversityCode universityCode
) {
}
