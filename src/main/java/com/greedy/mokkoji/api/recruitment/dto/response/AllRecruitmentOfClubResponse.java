package com.greedy.mokkoji.api.recruitment.dto.response;
import com.greedy.mokkoji.enums.recruitment.RecruitStatus;
import lombok.Builder;

import java.util.List;
import java.time.LocalDateTime;

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
            RecruitStatus status,
            LocalDateTime createdAt
    ) {}
}
