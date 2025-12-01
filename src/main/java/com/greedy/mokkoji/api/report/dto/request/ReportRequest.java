package com.greedy.mokkoji.api.report.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

public record ReportRequest(
        @Schema(example = "부적절한 댓글입니다.") String content
) {
}
