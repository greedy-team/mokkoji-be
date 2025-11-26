package com.greedy.mokkoji.api.comment.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "댓글 목록")
public record CommentResponse(
        @Schema(description = "댓글 ID", example = "1") Long id,
        @Schema(description = "댓글 내용", example = "최고의 동아리!") String content,
        @Schema(description = "평점", example = "5") Double rate,
        @Schema(description = "수정 여부", example = "true") boolean isModified,
        @Schema(description = "가장 최근 수정, 생성 날짜", example = "2025-08-22T00:00:00") LocalDateTime time,
        @Schema(description = "작성자 여부", example = "true") boolean isWriter
) {
    public static CommentResponse of(final Long id, final String content, final Double rate, final boolean isModified, final LocalDateTime time, final boolean isWriter) {
        return new CommentResponse(id, content, rate, isModified, time, isWriter);
    }
}
