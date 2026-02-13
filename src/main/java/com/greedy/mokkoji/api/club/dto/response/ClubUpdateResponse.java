package com.greedy.mokkoji.api.club.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
public record ClubUpdateResponse(
        @Schema(example = "1") Long id,
        @Schema(example = "https://mokkoji-app-data.s3.ap-northeast-2.amazonaws.com/club-logo/1/greedy_logo_{UUID}.jpg") String updateLogo,
        @Schema(example = "https://mokkoji-app-data.s3.ap-northeast-2.amazonaws.com/club-logo/1/old_logo_{UUID}.jpg") String deleteLogo
) {
    public static ClubUpdateResponse of(Long id, String updateLogo, String deleteLogo) {
        return new ClubUpdateResponse(id, updateLogo, deleteLogo);
    }
}
