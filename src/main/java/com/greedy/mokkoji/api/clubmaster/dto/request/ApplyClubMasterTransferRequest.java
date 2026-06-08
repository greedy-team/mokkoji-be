package com.greedy.mokkoji.api.clubmaster.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

public record ApplyClubMasterTransferRequest(
        @Schema(example = "1") Long clubId,
        @Schema(example = "김세종") String nextClubMasterName,
        @Schema(example = "~@gmail.com") String nextClubMasterEmail
) {
}
