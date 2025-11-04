package com.greedy.mokkoji.api.recruitment.dto.request;

import java.time.LocalDateTime;

public record UpdateRecruitmentRequest(
        String title,
        int imageCount,
        String content,
        LocalDateTime recruitStart,
        LocalDateTime recruitEnd,
        String recruitForm
) {
}

