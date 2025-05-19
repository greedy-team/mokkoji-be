package com.greedy.mokkoji.api.recruitment.dto.response;

import com.greedy.mokkoji.enums.recruitment.RecruitStatus;
import jakarta.persistence.Column;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Builder
public record AllRecruitmentOfClubResponse(
        List<Recruitment> recruitments
) {
    public static AllRecruitmentOfClubResponse of(List<Recruitment> recruitments) {
        return new AllRecruitmentOfClubResponse(recruitments);
    }

    public record Recruitment(
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
}
