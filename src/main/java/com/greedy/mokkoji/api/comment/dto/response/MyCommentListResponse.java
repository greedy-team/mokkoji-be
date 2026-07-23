package com.greedy.mokkoji.api.comment.dto.response;

import java.util.List;

public record MyCommentListResponse(
        List<MyCommentResponse> comments
) {
    public static MyCommentListResponse of(final List<MyCommentResponse> comments) {
        return new MyCommentListResponse(comments);
    }
}
