package com.greedy.mokkoji.api.recruitment.dto.response.deleteRecruitment;

import java.util.List;

public record DeleteRecruitmentResponse(
        Long recruitmentId,
        List<String> deleteImageUrls
) {
    public static DeleteRecruitmentResponse of(Long recruitmentId, List<String> deleteImageUrls) {
        return new DeleteRecruitmentResponse(recruitmentId, deleteImageUrls);
    }
}

