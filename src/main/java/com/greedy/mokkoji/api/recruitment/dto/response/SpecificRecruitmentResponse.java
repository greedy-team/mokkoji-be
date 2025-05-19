package com.greedy.mokkoji.api.recruitment.dto.response;

import lombok.Builder;

import java.time.LocalDate;
import java.util.List;

@Builder
public record SpecificRecruitmentResponse(
        Long id,
        String title,
        List<String> images,
        String content,
        LocalDate startDate,
        LocalDate endDate
) {
    public static SpecificRecruitmentResponse of(
            Long id,
            String title,
            List<String> images,
            String content,
            LocalDate startDate,
            LocalDate endDate
    ) {
        return new SpecificRecruitmentResponse(id, title, images, content, startDate, endDate);
    }
}
