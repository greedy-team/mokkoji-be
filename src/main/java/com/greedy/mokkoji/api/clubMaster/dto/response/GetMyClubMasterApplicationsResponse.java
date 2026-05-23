package com.greedy.mokkoji.api.clubMaster.dto.response;

import com.greedy.mokkoji.enums.application.ApplicationStatus;

import java.time.LocalDateTime;

public record GetMyClubMasterApplicationsResponse(
        Long id,
        String universityName,
        String clubName,
        String userName,
        ApplicationStatus status,
        String rejectReason,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
