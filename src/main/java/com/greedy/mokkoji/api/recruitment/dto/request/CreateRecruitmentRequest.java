package com.greedy.mokkoji.api.recruitment.dto.request;

import java.time.LocalDateTime;

public record CreateRecruitmentRequest(
        String title,
        int imageCount,
        String content,
        LocalDateTime recruitStart,
        LocalDateTime recruitEnd,
        String recruitForm,
        boolean isAlwaysRecruiting
) {
}
