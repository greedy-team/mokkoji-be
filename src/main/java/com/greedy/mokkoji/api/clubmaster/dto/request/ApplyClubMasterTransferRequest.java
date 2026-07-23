package com.greedy.mokkoji.api.clubmaster.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

public record ApplyClubMasterTransferRequest(
        @Schema(example = "1") Long clubId,
        @Schema(example = "X9y8Z7") String nextClubMasterUserCode
) {
}
