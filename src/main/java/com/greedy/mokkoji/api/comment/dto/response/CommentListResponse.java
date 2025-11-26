package com.greedy.mokkoji.api.comment.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "댓글 목록 응답")
public record CommentListResponse(
        @Schema(description = "댓글 목록") List<CommentResponse> comments
) {
    public static CommentListResponse of(final List<CommentResponse> comments) {
        return new CommentListResponse(comments);
    }
}
