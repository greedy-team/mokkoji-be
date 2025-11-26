package com.greedy.mokkoji.api.comment.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

@Schema(description = "댓글 작성 요청")
public record CommentCreateRequest(
        @Schema(description = "평점", example = "5") @Min(0) @Max(5) Double rate,
        @Schema(description = "댓글 내용", example = "최고의 동아리!") String content
) {
}
