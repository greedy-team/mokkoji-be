package com.greedy.mokkoji.api.comment.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record CommentUpdateRequest(
        @Min(0)
        @Max(5)
        Double rate,
        String content
) {
}
