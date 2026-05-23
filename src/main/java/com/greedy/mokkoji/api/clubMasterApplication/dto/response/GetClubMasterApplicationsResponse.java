package com.greedy.mokkoji.api.clubMasterApplication.dto.response;

import com.greedy.mokkoji.enums.application.ApplicationStatus;

import java.time.LocalDateTime;

public record GetClubMasterApplicationsResponse(
        Long id,
        String universityName,
        String clubName,
        String userName,
        ApplicationStatus status,
        String rejectReason,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {
}
