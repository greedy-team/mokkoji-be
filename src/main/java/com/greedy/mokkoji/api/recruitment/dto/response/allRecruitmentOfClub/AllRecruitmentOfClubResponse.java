package com.greedy.mokkoji.api.recruitment.dto.response.allRecruitmentOfClub;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.util.List;

@Builder
@Schema(description = "특정 동아리의 모집글 목록 응답")
public record AllRecruitmentOfClubResponse(
        @Schema(description = "모집글 목록") List<RecruitmentOfClubResponse> recruitments
) {
    public static AllRecruitmentOfClubResponse of(List<RecruitmentOfClubResponse> recruitments) {
        return new AllRecruitmentOfClubResponse(recruitments);
    }
}

