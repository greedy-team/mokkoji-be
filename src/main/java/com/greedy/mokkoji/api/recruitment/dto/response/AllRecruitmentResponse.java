package com.greedy.mokkoji.api.recruitment.dto.response;

import com.greedy.mokkoji.enums.recruitment.RecruitStatus;

import java.time.LocalDate;
import java.util.List;

public record AllRecruitmentResponse(
        List<AllRecruitmentResponse.Recruitment> recruitments
) {
    public static AllRecruitmentResponse of(List<Recruitment> recruitments) {
        return new AllRecruitmentResponse(recruitments);
    }

    public record Recruitment(
            Long clubId,
            Long recruitmentId,
            String title,
            RecruitStatus status,
            LocalDate startDate,
            LocalDate endDate,
            String clubName
    ) {
    }
}

