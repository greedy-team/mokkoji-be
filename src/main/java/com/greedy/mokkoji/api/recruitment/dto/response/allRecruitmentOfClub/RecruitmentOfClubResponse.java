package com.greedy.mokkoji.api.recruitment.dto.response.allRecruitmentOfClub;

import com.greedy.mokkoji.enums.recruitment.RecruitStatus;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record RecruitmentOfClubResponse(
        Long id,
        String title,
        String content,
        LocalDateTime recruitStart,
        LocalDateTime recruitEnd,
        RecruitStatus status,
        LocalDateTime createdAt,
        String firstImage
) {
}
