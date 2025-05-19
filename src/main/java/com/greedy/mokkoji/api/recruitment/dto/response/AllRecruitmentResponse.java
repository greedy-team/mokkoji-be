package com.greedy.mokkoji.api.recruitment.dto.response;

import com.greedy.mokkoji.enums.recruitment.RecruitStatus;
import com.greedy.mokkoji.api.club.dto.page.PageResponse;

import java.time.LocalDateTime;
import java.util.List;

public record AllRecruitmentResponse(
        List<AllRecruitmentResponse.Recruitment> recruitments,
        PageResponse page
) {
    public static AllRecruitmentResponse of(List<Recruitment> recruitments, PageResponse page) {
        return new AllRecruitmentResponse(recruitments, page);
    }

    public record Recruitment(
            Long clubId,
            String clubName,
            Long id,
            String title,
            LocalDateTime recruitStart,
            LocalDateTime recruitEnd,
            RecruitStatus status,
            String firstImage,
            boolean isFavorite
    ) {
    }
}
