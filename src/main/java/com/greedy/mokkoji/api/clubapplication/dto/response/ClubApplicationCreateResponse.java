package com.greedy.mokkoji.api.clubapplication.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record ClubApplicationCreateResponse(
        @Schema(example = "1") Long applicationId,
        @Schema(example = "https://mokkoji-app-data.s3.ap-northeast-2.amazonaws.com/club-application-logo/1/greedy_logo_{UUID}.jpg") String uploadLogoUrl
) {
    public static ClubApplicationCreateResponse of(final Long applicationId, final String uploadLogoUrl) {
        return new ClubApplicationCreateResponse(applicationId, uploadLogoUrl);
    }
}
