package com.greedy.mokkoji.api.clubapplication.dto.response;

import com.greedy.mokkoji.db.clubapplication.entity.ClubApplication;
import com.greedy.mokkoji.enums.application.ApplicationStatus;
import com.greedy.mokkoji.enums.club.ClubAffiliation;
import com.greedy.mokkoji.enums.club.ClubCategory;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

public record AdminClubApplicationResponse(
        @Schema(example = "1") Long applicationId,
        @Schema(example = "세종대학교") String universityName,
        @Schema(example = "그리디") String clubName,
        @Schema(example = "홍길동") String applicantName,
        @Schema(example = "ACADEMIC_CULTURAL") ClubCategory category,
        @Schema(example = "CENTRAL_CLUB") ClubAffiliation affiliation,
        @Schema(example = "https://s3.amazonaws.com/.../logo.png") String logo,
        @Schema(example = "REJECTED") ApplicationStatus status,
        @Schema(example = "서류 미비") String rejectReason,
        @Schema(example = "2026-05-18T13:00:00") LocalDateTime createdAt
) {
    public static AdminClubApplicationResponse from(final ClubApplication clubApplication) {
        return new AdminClubApplicationResponse(
                clubApplication.getId(),
                clubApplication.getUniversity().getName(),
                clubApplication.getClubName(),
                clubApplication.getApplicantName(),
                clubApplication.getClubCategory(),
                clubApplication.getClubAffiliation(),
                clubApplication.getLogo(),
                clubApplication.getStatus(),
                clubApplication.getRejectReason(),
                clubApplication.getCreatedAt()
        );
    }
}
