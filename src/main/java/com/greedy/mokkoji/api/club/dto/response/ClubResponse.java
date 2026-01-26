package com.greedy.mokkoji.api.club.dto.response;

import com.greedy.mokkoji.enums.club.ClubAffiliation;
import com.greedy.mokkoji.enums.club.ClubCategory;
import com.greedy.mokkoji.enums.recruitment.RecruitStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Builder
public record ClubResponse(
        @Schema(example = "1") Long id,
        @Schema(example = "그리디") String name,
        @Schema(example = "ACADEMIC_CULTURAL") ClubCategory category,
        @Schema(example = "DEPARTMENT_CLUB") ClubAffiliation affiliation,
        @Schema(example = "세종대 최고의 코딩 동아리") String description,
        @Schema(example = "2025-11-25") String recruitStartDate,
        @Schema(example = "2025-12-04") String recruitEndDate,
        @Schema(example = "false") Boolean isAlwaysRecruiting,
        @Schema(example = "OPEN") RecruitStatus recruitStatus,
        @Schema(example = "https://mokkoji-app-data.s3.ap-northeast-2.amazonaws.com/club-logo/1/greedy_{UUID}.jpg") String logo,
        @Schema(example = "true") Boolean isFavorite
) {
    public static ClubResponse of(
            final Long id,
            final String name,
            final ClubCategory category,
            final ClubAffiliation affiliation,
            final String description,
            final LocalDateTime recruitStartDate,
            final LocalDateTime recruitEndDate,
            final Boolean isAlwaysRecruiting,
            final RecruitStatus recruitStatus,
            final String logo,
            final Boolean isFavorite) {

        return ClubResponse.builder()
                .id(id)
                .name(name)
                .category(category)
                .affiliation(affiliation)
                .description(description)
                .recruitStartDate(recruitStartDate != null ? recruitStartDate.format(DateTimeFormatter.ISO_LOCAL_DATE) : null)
                .recruitEndDate(recruitEndDate != null ? recruitEndDate.format(DateTimeFormatter.ISO_LOCAL_DATE) : null)
                .isAlwaysRecruiting(isAlwaysRecruiting)
                .recruitStatus(recruitStatus)
                .logo(logo)
                .isFavorite(isFavorite)
                .build();
    }
}
