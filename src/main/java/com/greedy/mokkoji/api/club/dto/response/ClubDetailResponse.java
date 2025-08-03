package com.greedy.mokkoji.api.club.dto.response;

import lombok.Builder;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Builder
public record ClubDetailResponse(
        Long id,
        String name,
        String category,
        String affiliation,
        String description,
        String recruitStartDate,
        String recruitEndDate,
        String logo,
        Boolean isFavorite,
        String instagram,
        String recruitPost
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
                .recruitStartDate(recruitStartDate.format(DateTimeFormatter.ISO_LOCAL_DATE))
                .recruitEndDate(recruitEndDate.format(DateTimeFormatter.ISO_LOCAL_DATE))
                .logo(logo)
                .isFavorite(isFavorite)
                .instagram(instagram)
                .recruitPost(recruitPost)
                .build();
    }
}
