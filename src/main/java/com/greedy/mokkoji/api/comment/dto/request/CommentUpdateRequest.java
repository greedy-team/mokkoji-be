package com.greedy.mokkoji.api.comment.dto.request;

public record CommentUpdateRequest(
        Double rate,
        String content
) {
}
