package com.greedy.mokkoji.api.comment.dto.response;

import java.util.List;

public record CommentListResponse(
        List<CommentResponse> comments
) {
    public static CommentListResponse of(final List<CommentResponse> comments) {
        return new CommentListResponse(comments);
    }
}
