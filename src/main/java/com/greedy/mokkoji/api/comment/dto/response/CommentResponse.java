package com.greedy.mokkoji.api.comment.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

public record CommentResponse(
        @Schema(example = "1") Long id,
        @Schema(example = "최고의 동아리!") String content,
        @Schema(example = "5") Double rate,
        @Schema(example = "true") boolean isModified,
        @Schema(example = "2025-08-22T00:00:00") LocalDateTime time,
        @Schema(example = "true") boolean isWriter
) {
    public static CommentResponse of(final Long id, final String content, final Double rate, final boolean isModified, final LocalDateTime time, final boolean isWriter) {
        return new CommentResponse(id, content, rate, isModified, time, isWriter);
    }
}
