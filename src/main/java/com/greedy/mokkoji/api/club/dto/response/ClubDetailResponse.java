package com.greedy.mokkoji.api.club.dto.response;

import com.greedy.mokkoji.enums.club.ClubAffiliation;
import com.greedy.mokkoji.enums.club.ClubCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Builder
public record ClubDetailResponse(
        @Schema(example = "1") Long id,
        @Schema(example = "그리디") String name,
        @Schema(example = "ACADEMIC_CULTURAL") ClubCategory category,
        @Schema(example = "DEPARTMENT_CLUB") ClubAffiliation affiliation,
        @Schema(example = "세종대 최고의 코딩 동아리") String description,
        @Schema(example = "2025-11-25") String recruitStartDate,
        @Schema(example = "2025-12-04") String recruitEndDate,
        @Schema(example = "https://mokkoji-app-data.s3.ap-northeast-2.amazonaws.com/club-logo/1/greedy_{UUID}.jpg") String logo,
        @Schema(example = "true") Boolean isFavorite,
        @Schema(example = "https://instagram.com/greedy_club") String instagram,
        @Schema(example = "세종대학교 개발 동아리에서 신입 회원을 모집합니다!") String recruitPost,
        @Schema(description = "수집 시작일부터 DB에 반영된 일별 조회수의 합계. 별도 조회수 수집 API의 증가분은 배치 반영 후 포함됩니다.", example = "1250") long viewCount
) {
    public static ClubDetailResponse of(
            final Long id,
            final String name,
            final ClubCategory category,
            final ClubAffiliation affiliation,
            final String description,
            final LocalDateTime recruitStartDate,
            final LocalDateTime recruitEndDate,
            final String logo,
            final Boolean isFavorite,
            final String instagram,
            final String recruitPost,
            final long viewCount
    ) {

        return ClubDetailResponse.builder()
                .id(id)
                .name(name)
                .category(category)
                .affiliation(affiliation)
                .description(description)
                .recruitStartDate(recruitStartDate != null ? recruitStartDate.format(DateTimeFormatter.ISO_LOCAL_DATE) : null)
                .recruitEndDate(recruitEndDate != null ? recruitEndDate.format(DateTimeFormatter.ISO_LOCAL_DATE) : null)
                .logo(logo)
                .isFavorite(isFavorite)
                .instagram(instagram)
                .recruitPost(recruitPost)
                .viewCount(viewCount)
                .build();
    }
}
