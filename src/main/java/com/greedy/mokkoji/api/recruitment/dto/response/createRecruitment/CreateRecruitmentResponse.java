package com.greedy.mokkoji.api.recruitment.dto.response.createRecruitment;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "모집글 생성 응답")
public record CreateRecruitmentResponse(
        @Schema(description = "생성된 모집글 ID", example = "1") Long id,
        @Schema(description = "이미지 업로드용 Presigned URL 리스트") List<String> uploadImageUrls
) {
    public static CreateRecruitmentResponse of(Long id, List<String> uploadImageUrls) {
        return new CreateRecruitmentResponse(id, uploadImageUrls);
    }
}
