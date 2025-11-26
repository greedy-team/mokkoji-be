package com.greedy.mokkoji.api.club.dto.request;

import com.greedy.mokkoji.enums.club.ClubAffiliation;
import com.greedy.mokkoji.enums.club.ClubCategory;
import com.greedy.mokkoji.enums.recruitment.RecruitStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "동아리 검색 조건")
public record ClubSearchCond(
        @Schema(description = "검색 키워드", example = "그리디") String keyword,
        @Schema(description = "동아리 카테고리", example = "ACADEMIC_CULTURAL") ClubCategory category,
        @Schema(description = "동아리 소속", example = "DEPARTMENT_CLUB") ClubAffiliation affiliation,
        @Schema(description = "모집 상태", example = "OPEN") RecruitStatus recruitStatus
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
