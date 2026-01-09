package com.greedy.mokkoji.api.club.dto.request;

import com.greedy.mokkoji.enums.club.ClubAffiliation;
import com.greedy.mokkoji.enums.club.ClubCategory;
import com.greedy.mokkoji.enums.recruitment.RecruitStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
public record ClubSearchCond(
        @Schema(example = "그리디") String keyword,
        @Schema(example = "ACADEMIC_CULTURAL") ClubCategory category,
        @Schema(example = "DEPARTMENT_CLUB") ClubAffiliation affiliation,
        @Schema(example = "OPEN") RecruitStatus recruitStatus
) {
    public static ClubSearchCond of(final String keyword, final ClubCategory category, final ClubAffiliation affiliation, final RecruitStatus recruitStatus) {
        return ClubSearchCond.builder()
                .keyword(keyword)
                .category(category)
                .affiliation(affiliation)
                .recruitStatus(recruitStatus)
                .build();
    }
}
