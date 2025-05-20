package com.greedy.mokkoji.api.recruitment.dto.response.AllRecruitment;

import com.greedy.mokkoji.enums.recruitment.RecruitStatus;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record RecruitmentPreviewResponse(
        ClubPreviewResponse club,
        Long id,
        String title,
        LocalDateTime recruitStart,
        LocalDateTime recruitEnd,
        RecruitStatus status,
        boolean isFavorite
) {
}
