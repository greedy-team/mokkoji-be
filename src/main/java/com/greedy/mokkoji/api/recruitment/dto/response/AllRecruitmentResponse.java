package com.greedy.mokkoji.api.recruitment.dto.response;

import java.time.LocalDate;
import java.util.List;

public record AllRecruitmentResponse (
        List<AllRecruitmentOfClubResponse.Recruitment> recruitments
) {
    public static AllRecruitmentOfClubResponse of(List<AllRecruitmentOfClubResponse.Recruitment> recruitments) {
        return new AllRecruitmentOfClubResponse(recruitments);
    }
    public record Recruitment(
            Long clubId,
            Long recruitmentId,
            String title,
            boolean isRecruiting,
            LocalDate startDate,
            LocalDate endDate,
            String clubName
    ) {}
}
