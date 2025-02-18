package com.greedy.mokkoji.api.club.dto.club;

import com.greedy.mokkoji.db.club.entity.Club;
import com.greedy.mokkoji.db.recruitment.entity.Recruitment;
import lombok.Builder;

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
    public static ClubDetailResponse of(final Club club, final Recruitment recruitment, final Boolean isFavorite) {
        return ClubDetailResponse.builder()
                .id(club.getId())
                .name(club.getName())
                .category(club.getClubCategory().getDescription().toString())
                .affiliation(club.getClubAffiliation().getDescription().toString())
                .description(club.getDescription())
                .recruitStartDate(recruitment.getRecruitStart().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")))
                .recruitEndDate(recruitment.getRecruitEnd().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")))
                .imageURL(club.getLogo())
                .isFavorite(isFavorite)
                .instagramLink(club.getInstagram())
                .recruitPost(recruitment.getContent())
                .build();
    }
}
