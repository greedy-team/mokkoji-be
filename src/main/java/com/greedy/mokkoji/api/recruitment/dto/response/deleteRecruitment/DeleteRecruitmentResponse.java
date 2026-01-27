package com.greedy.mokkoji.api.recruitment.dto.response.deleteRecruitment;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record DeleteRecruitmentResponse(
        @Schema(example = "1") Long id,
        @Schema(example = "[\"https://mokkoji-app-data.s3.ap-northeast-2.amazonaws.com/recruitment-image/1/2/{UUID}.jpg\"]") List<String> deleteImageUrls
) {
    public static DeleteRecruitmentResponse of(Long id, List<String> deleteImageUrls) {
        return new DeleteRecruitmentResponse(id, deleteImageUrls);
    }
}

