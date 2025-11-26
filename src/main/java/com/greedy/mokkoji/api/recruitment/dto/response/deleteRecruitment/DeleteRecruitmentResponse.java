package com.greedy.mokkoji.api.recruitment.dto.response.deleteRecruitment;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "모집글 삭제 응답")
public record DeleteRecruitmentResponse(
        @Schema(description = "삭제된 모집글 ID", example = "1") Long id,
        @Schema(description = "이미지 삭제용 Presigned URL 리스트") List<String> deleteImageUrls
) {
    public static DeleteRecruitmentResponse of(Long id, List<String> deleteImageUrls) {
        return new DeleteRecruitmentResponse(id, deleteImageUrls);
    }
}

