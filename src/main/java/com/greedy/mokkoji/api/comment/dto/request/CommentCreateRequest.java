package com.greedy.mokkoji.api.comment.dto.request;

public record CommentCreateRequest(
        Double rate,
        String content
) {
}
