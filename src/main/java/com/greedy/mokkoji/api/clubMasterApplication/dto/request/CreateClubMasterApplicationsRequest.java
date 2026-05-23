package com.greedy.mokkoji.api.clubMasterApplication.dto.request;

import com.greedy.mokkoji.enums.university.UniversityCode;
import io.swagger.v3.oas.annotations.media.Schema;

public record CreateClubMasterApplicationsRequest(
        @Schema(example = "SEJONG") UniversityCode universityCode,
        @Schema(example = "1") Long clubId,
        @Schema(example = "김세종") String userName
) {
}
