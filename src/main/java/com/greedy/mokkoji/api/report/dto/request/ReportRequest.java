package com.greedy.mokkoji.api.report.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "신고 요청")
public record ReportRequest(
        @Schema(description = "신고 내용", example = "부적절한 댓글입니다.") String content
) {
}
