package com.greedy.mokkoji.api.clubapplication.dto.response;

import com.greedy.mokkoji.db.clubapplication.entity.ClubApplication;
import com.greedy.mokkoji.enums.clubApplication.ClubApplicationStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

public record ClubApplicationResponse(
        @Schema(example = "1") Long clubApplicationId,
        @Schema(example = "세종대학교") String universityName,
        @Schema(example = "그리디") String clubName,
        @Schema(example = "PENDING") ClubApplicationStatus status,
        @Schema(example = "서류 미비") String rejectReason,
        @Schema(example = "2026-05-18T13:00:00") LocalDateTime createdAt
) {
    public static ClubApplicationResponse from(final ClubApplication clubApplication) {
        return new ClubApplicationResponse(
                clubApplication.getId(),
                clubApplication.getUniversity().getName(),
                clubApplication.getClubName(),
                clubApplication.getStatus(),
                clubApplication.getRejectReason(),
                clubApplication.getCreatedAt()
        );
    }
}
