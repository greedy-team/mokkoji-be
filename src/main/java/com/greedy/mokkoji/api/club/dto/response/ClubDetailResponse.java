package com.greedy.mokkoji.api.club.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Builder
public record ClubDetailResponse(
        @Schema(example = "1") Long id,
        @Schema(example = "그리디") String name,
        @Schema(example = "학술/교양") String category,
        @Schema(example = "정인준/가인준") String affiliation,
        @Schema(example = "세종대 최고의 코딩 동아리") String description,
        @Schema(example = "2025-11-25") String recruitStartDate,
        @Schema(example = "2025-12-04") String recruitEndDate,
        @Schema(example = "https://mokkoji-app-data.s3.ap-northeast-2.amazonaws.com/club-logo/1/greedy_{UUID}.jpg") String logo,
        @Schema(example = "true") Boolean isFavorite,
        @Schema(example = "https://instagram.com/greedy_club") String instagram,
        @Schema(example = "세종대학교 개발 동아리에서 신입 회원을 모집합니다!") String recruitPost
) {
    public static ClubDetailResponse of(
            final Long id,
            final String name,
            final String category,
            final String affiliation,
            final String description,
            final LocalDateTime recruitStartDate,
            final LocalDateTime recruitEndDate,
            final String logo,
            final Boolean isFavorite,
            final String instagram,
            final String recruitPost
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
                .build();
    }
}
