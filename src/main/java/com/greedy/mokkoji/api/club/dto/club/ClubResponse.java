package com.greedy.mokkoji.api.club.dto.club;

import lombok.Builder;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

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
            final LocalDateTime recruitStartDate,
            final LocalDateTime recruitEndDate,
            final String imageURL,
            final Boolean isFavorite) {

        return ClubResponse.builder()
                .id(id)
                .name(name)
                .category(category)
                .affiliation(affiliation)
                .description(description)
                .recruitStartDate(recruitStartDate.format(DateTimeFormatter.ISO_LOCAL_DATE))
                .recruitEndDate(recruitEndDate.format(DateTimeFormatter.ISO_LOCAL_DATE))
                .imageURL(imageURL)
                .isFavorite(isFavorite)
                .build();
    }
}
