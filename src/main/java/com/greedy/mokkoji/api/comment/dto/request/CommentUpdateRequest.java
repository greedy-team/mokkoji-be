package com.greedy.mokkoji.api.comment.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record CommentUpdateRequest(
        @Schema(example = "5", minimum = "0", maximum = "5") @Min(0) @Max(5) Double rate,
        @Schema(example = "최고의 동아리!") String content
) {
}
