package com.greedy.mokkoji.api.recruitment.dto.request;

import java.time.LocalDateTime;
import java.util.List;

public record CreateRecruitmentRequest(
        String title,
        List<String> imageNames,
        String content,
        LocalDateTime recruitStart,
        LocalDateTime recruitEnd,
        String recruitForm,
        boolean isAlwaysRecruiting
) {
}
