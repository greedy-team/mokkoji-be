package com.greedy.mokkoji.api.recruitment.dto.response.updateRecruitment;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record UpdateRecruitmentResponse(
        @Schema(example = "1") Long id,
        @Schema(example = "[\"https://mokkoji-app-data.s3.ap-northeast-2.amazonaws.com/recruitment-image/1/2/{UUID}.jpg\"]") List<String> deleteImageUrls,
        @Schema(example = "[\"https://mokkoji-app-data.s3.ap-northeast-2.amazonaws.com/recruitment-image/1/2/{UUID}.jpg\"]") List<String> uploadImageUrls
) {
    public static UpdateRecruitmentResponse of(Long id, List<String> deleteImageUrls, List<String> uploadImageUrls) {
        return new UpdateRecruitmentResponse(id, deleteImageUrls, uploadImageUrls);
    }
}

