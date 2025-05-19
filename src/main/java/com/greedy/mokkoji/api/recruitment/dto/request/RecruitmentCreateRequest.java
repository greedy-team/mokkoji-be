package com.greedy.mokkoji.api.recruitment.dto.request;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Builder
public record RecruitmentCreateRequest(
        String title,
        List<String> images,
        String content,
        LocalDateTime startDate,
        LocalDateTime endDate
) {
}
