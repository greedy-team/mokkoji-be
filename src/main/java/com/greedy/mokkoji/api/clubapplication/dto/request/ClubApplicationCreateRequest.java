package com.greedy.mokkoji.api.clubapplication.dto.request;

import com.greedy.mokkoji.enums.club.ClubAffiliation;
import com.greedy.mokkoji.enums.club.ClubCategory;
import com.greedy.mokkoji.enums.university.UniversityCode;
import io.swagger.v3.oas.annotations.media.Schema;

public record ClubApplicationCreateRequest(
        @Schema(example = "SEJONG") UniversityCode universityCode,
        @Schema(example = "그리디") String clubName,
        @Schema(example = "홍길동") String applicantName,
        @Schema(example = "ACADEMIC_CULTURAL") ClubCategory clubCategory,
        @Schema(example = "DEPARTMENT_CLUB") ClubAffiliation clubAffiliation,
        @Schema(example = "logo/path.jpg") String logo,
        @Schema(example = "https://instagram.com/greedy") String instagram,
        @Schema(example = "알고리즘을 공부하는 동아리입니다.") String description
) {
}
