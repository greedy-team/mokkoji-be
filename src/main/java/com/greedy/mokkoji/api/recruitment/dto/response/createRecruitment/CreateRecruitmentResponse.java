package com.greedy.mokkoji.api.recruitment.dto.response.createRecruitment;

import java.util.List;

public record CreateRecruitmentResponse(
        Long id,
        List<String> uploadImageUrls
) {
    public static CreateRecruitmentResponse of(Long id, List<String> uploadImageUrls) {
        return new CreateRecruitmentResponse(id, uploadImageUrls);
    }
}
