package com.greedy.mokkoji.api.comment.dto.response;

import java.time.LocalDateTime;

public record CommentResponse(
        Long id,
        String content,
        Double rate,
        boolean isModified,
        LocalDateTime time,
        boolean isWriter
) {
    public static CommentResponse of(final Long id, final String content, final Double rate, final boolean isModified, final LocalDateTime time, final boolean isWriter) {
        return new CommentResponse(id, content, rate, isModified, time, isWriter);
    }
}
