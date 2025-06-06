package com.greedy.mokkoji.api.recruitment.dto.response.specificRecruitment;

import com.greedy.mokkoji.enums.recruitment.RecruitStatus;

import java.time.LocalDateTime;
import java.util.List;

public record SpecificRecruitmentResponse(
        Long id,
        String title,
        String content,
        LocalDateTime recruitStart,
        LocalDateTime recruitEnd,
        RecruitStatus status,
        LocalDateTime createdAt,
        List<String> imageUrls,
        String recruitForm
) {
    public static SpecificRecruitmentResponse of(
            Long id,
            String title,
            String content,
            LocalDateTime recruitStart,
            LocalDateTime recruitEnd,
            RecruitStatus status,
            LocalDateTime createdAt,
            List<String> imageUrls,
            String recruitForm
    ) {
        return new SpecificRecruitmentResponse(id, title, content, recruitStart, recruitEnd, status, createdAt, imageUrls, recruitForm);
    }
}
