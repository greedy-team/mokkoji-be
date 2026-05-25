package com.greedy.mokkoji.api.clubmaster.dto.response;

import com.greedy.mokkoji.enums.application.ApplicationStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

public record GetMyClubMasterApplicationsResponse(
        @Schema(example = "1") Long id,
        @Schema(example = "세종대") String universityName,
        @Schema(example = "그리디") String clubName,
        @Schema(example = "김세종") String userName,
        @Schema(example = "PENDING") ApplicationStatus status,
        @Schema(example = "실명을 적어주세요.") String rejectReason,
        @Schema(example = "2026-04-22T14:00:00") LocalDateTime createdAt,
        @Schema(example = "2026-04-23T14:00:00") LocalDateTime updatedAt
) {
}
