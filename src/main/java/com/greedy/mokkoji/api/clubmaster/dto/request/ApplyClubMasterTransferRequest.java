package com.greedy.mokkoji.api.clubmaster.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

public record ApplyClubMasterTransferRequest(
        @Schema(example = "1") Long clubId,
        @Schema(example = "550e8400-e29b-41d4-a716-446655440000") String nextClubMasterUserCode
) {
}
