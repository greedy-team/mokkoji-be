package com.greedy.mokkoji.api.recruitment.dto.response.updateRecruitment;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "모집글 수정 응답")
public record UpdateRecruitmentResponse(
        @Schema(description = "수정된 모집글 ID", example = "1") Long id,
        @Schema(description = "이미지 삭제용 Presigned URL 리스트") List<String> deleteImageUrls,
        @Schema(description = "이미지 업로드용 Presigned URL 리스트") List<String> uploadImageUrls
) {
    public static UpdateRecruitmentResponse of(Long id, List<String> deleteImageUrls, List<String> uploadImageUrls) {
        return new UpdateRecruitmentResponse(id, deleteImageUrls, uploadImageUrls);
    }
}

