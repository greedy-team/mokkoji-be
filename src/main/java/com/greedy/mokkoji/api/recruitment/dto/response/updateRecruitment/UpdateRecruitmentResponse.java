package com.greedy.mokkoji.api.recruitment.dto.response.updateRecruitment;

import java.util.List;

public record UpdateRecruitmentResponse(
        Long recruitmentId,
        List<String> deleteImageUrls,
        List<String> uploadImageUrls
) {
    public static UpdateRecruitmentResponse of(Long recruitmentId, List<String> deleteImageUrls, List<String> uploadImageUrls) {
        return new UpdateRecruitmentResponse(recruitmentId, deleteImageUrls, uploadImageUrls);
    }
}

