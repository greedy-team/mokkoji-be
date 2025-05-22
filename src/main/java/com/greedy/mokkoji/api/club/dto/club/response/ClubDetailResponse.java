package com.greedy.mokkoji.api.club.dto.club.response;

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
        String imageURL,
        Boolean isFavorite,
        String instagramLink,
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
            final String imageURL,
            final Boolean isFavorite,
            final String instagramLink,
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
                .imageURL(imageURL)
                .isFavorite(isFavorite)
                .instagramLink(instagramLink)
                .recruitPost(recruitPost)
                .build();
    }
}
