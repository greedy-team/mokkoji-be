package com.greedy.mokkoji.api.club.dto.response;

import lombok.Builder;

@Builder
public record ClubManageDetailResponse(
        String name,
        String category,
        String affiliation,
        String description,
        String logo,
        String instagram
) {
    public static ClubManageDetailResponse of(final String name, final String category, final String affiliation, final String description, final String logo, final String instagram) {
        return new ClubManageDetailResponse(name, category, affiliation, description, logo, instagram);
    }
}
