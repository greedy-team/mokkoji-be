package com.greedy.mokkoji.api.recruitment.dto.response.updateRecruitment;

import java.util.List;

public record UpdateRecruitmentResponse(
        Long id,
        List<String> deleteImageUrls,
        List<String> uploadImageUrls
) {
    public static UpdateRecruitmentResponse of(Long id, List<String> deleteImageUrls, List<String> uploadImageUrls) {
        return new UpdateRecruitmentResponse(id, deleteImageUrls, uploadImageUrls);
    }
}

