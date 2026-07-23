package com.greedy.mokkoji.api.clubapplication.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

public record ClubApplicationRejectRequest(
        @Schema(example = "동아리 서류 미비") String rejectReason
) {
}
