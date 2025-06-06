package com.greedy.mokkoji.api.club.dto.club.response;

import lombok.Builder;

@Builder
public record ClubUpdateResponse(
        String updateLogo,
        String deleteLogo
) {
    public static ClubUpdateResponse of(String updateLogo, String deleteLogo) {
        return new ClubUpdateResponse(updateLogo, deleteLogo);
    }
}
