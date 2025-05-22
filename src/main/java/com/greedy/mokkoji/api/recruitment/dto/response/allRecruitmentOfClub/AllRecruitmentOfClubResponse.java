package com.greedy.mokkoji.api.recruitment.dto.response.allRecruitmentOfClub;


import lombok.Builder;

import java.util.List;

@Builder
public record AllRecruitmentOfClubResponse(
        List<RecruitmentOfClubResponse> recruitments
) {
    public static AllRecruitmentOfClubResponse of(List<RecruitmentOfClubResponse> recruitments) {
        return new AllRecruitmentOfClubResponse(recruitments);
    }
}

