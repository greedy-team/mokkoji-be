package com.greedy.mokkoji.api.recruitment.dto.response.updateRecruitment;

import java.util.List;

public record UpdateRecruitmentResponse(
        Long recruitmentId,
        List<String> imageUrls
) {
    public static UpdateRecruitmentResponse of(Long recruitmentId, List<String> imageUrls) {
        return new UpdateRecruitmentResponse(recruitmentId, imageUrls);
    }
}
