package com.greedy.mokkoji.api.feedback.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record FeedbackRequest(
        @Schema(example = "5", minimum = "0", maximum = "5") @Min(0) @Max(5) int rating,
        @Schema(example = "부적절한 댓글입니다.") String content
) {
}
