package com.greedy.mokkoji.api.recruitment.dto.response.createRecruitment;

import lombok.Builder;

import java.util.List;

@Builder
public record CreateRecruitmentResponse(
        Long id,
        List<String> imageUrls
) {
    public static CreateRecruitmentResponse of(Long id, List<String> imageUrls) {
        return new CreateRecruitmentResponse(id, imageUrls);
    }
}
