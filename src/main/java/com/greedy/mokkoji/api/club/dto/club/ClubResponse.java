package com.greedy.mokkoji.api.club.dto.club;

import lombok.Builder;

@Builder
public record ClubResponse(
        Long id,
        String name,
        String category,
        String affiliation,
        String description,
        String recruitStartDate,
        String recruitEndDate,
        String imageURL,
        Boolean isFavorite
) {
    public static ClubResponse of(
            final Long id,
            final String name,
            final String category,
            final String affiliation,
            final String description,
            final String recruitStartDate,
            final String recruitEndDate,
            final String imageURL,
            final Boolean isFavorite) {

        return ClubResponse.builder()
                .id(id)
                .name(name)
                .category(category)
                .affiliation(affiliation)
                .description(description)
                .recruitStartDate(recruitStartDate)
                .recruitEndDate(recruitEndDate)
                .imageURL(imageURL)
                .isFavorite(isFavorite)
                .build();
    }
}
