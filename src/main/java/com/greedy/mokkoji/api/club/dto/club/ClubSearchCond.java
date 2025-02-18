package com.greedy.mokkoji.api.club.dto.club;

import com.greedy.mokkoji.enums.ClubAffiliation;
import com.greedy.mokkoji.enums.ClubCategory;
import com.greedy.mokkoji.enums.RecruitStatus;
import lombok.Builder;

@Builder
public record ClubSearchCond(
        String keyword,
        ClubCategory category,
        ClubAffiliation affiliation,
        RecruitStatus recruitStatus
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
