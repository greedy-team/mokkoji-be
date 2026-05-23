package com.greedy.mokkoji.api.admin.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

public record RejectClubMasterApplicationRequest(
        @Schema(example = "실명을 적어주세요.") String rejectReason
) {
}
