package com.greedy.mokkoji.api.comment.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

public record MyCommentResponse(
        @Schema(example = "1") Long commentId,
        @Schema(example = "1") Long clubId,
        @Schema(example = "그리디") String name,
        @Schema(example = "개발 생태계에 선한 영향력을") String description,
        @Schema(example = "2025-08-22T00:00:00") LocalDateTime createdAt
) {
    public static MyCommentResponse of(final Long commentId, final Long clubId, final String name, final String description, final LocalDateTime createdAt) {
        return new MyCommentResponse(commentId, clubId, name, description, createdAt);
    }
}
