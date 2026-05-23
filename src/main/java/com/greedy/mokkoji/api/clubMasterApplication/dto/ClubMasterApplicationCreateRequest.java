package com.greedy.mokkoji.api.clubMasterApplication.dto;

import com.greedy.mokkoji.enums.university.UniversityCode;
import io.swagger.v3.oas.annotations.media.Schema;

public record ClubMasterApplicationCreateRequest(
        @Schema(example = "SEJONG") UniversityCode universityCode,
        @Schema(example = "1") Long clubId,
        @Schema(example = "김세종") String userName
) {
}
