package com.greedy.mokkoji.api.recruitment.dto.response;

import com.greedy.mokkoji.enums.recruitment.RecruitStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record AllRecruitmentResponse(
        List<AllRecruitmentResponse.Recruitment> recruitments
) {
    public static AllRecruitmentResponse of(List<Recruitment> recruitments) {
        return new AllRecruitmentResponse(recruitments);
    }

    public record Recruitment(
            Long clubId,
            String clubName,
            Long id,
            String title,
            LocalDateTime recruitStart,
            LocalDateTime recruitEnd,
            RecruitStatus status,
            String firstImage
    ) {
    }
}

