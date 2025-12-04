package com.greedy.mokkoji.api.recruitment.dto.response.createRecruitment;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record CreateRecruitmentResponse(
        @Schema(example = "1") Long id,
        @Schema(example = "[\"https://mokkoji-app-data.s3.ap-northeast-2.amazonaws.com/recruitment-image/1/2/{UUID}.jpg\"]") List<String> uploadImageUrls
) {
    public static CreateRecruitmentResponse of(Long id, List<String> uploadImageUrls) {
        return new CreateRecruitmentResponse(id, uploadImageUrls);
    }
}
