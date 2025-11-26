package com.greedy.mokkoji.api.club.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Builder
@Schema(description = "동아리 상세정보 조회 응답")
public record ClubDetailResponse(
        @Schema(description = "동아리 ID", example = "1") Long id,
        @Schema(description = "동아리명", example = "그리디") String name,
        @Schema(description = "동아리 카테고리", example = "학술/교양") String category,
        @Schema(description = "동아리 소속", example = "정인준/가인준") String affiliation,
        @Schema(description = "동아리 소개", example = "세종대 최고의 코딩 동아리") String description,
        @Schema(description = "모집 시작일", example = "2025-11-25") String recruitStartDate,
        @Schema(description = "모집 종료일", example = "2025-12-04") String recruitEndDate,
        @Schema(description = "동아리 로고", example = "greedy_logo.jpg") String logo,
        @Schema(description = "즐겨찾기 여부", example = "true") Boolean isFavorite,
        @Schema(description = "인스타그램 URL", example = "https://instagram.com/greedy_club") String instagram,
        @Schema(description = "모집글 내용", example = "세종대학교 개발 동아리에서 신입 회원을 모집합니다!") String recruitPost
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
