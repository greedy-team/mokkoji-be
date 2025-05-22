package com.greedy.mokkoji.api.recruitment.dto.response.deleteRecruitment;

import java.util.List;

public record DeleteRecruitmentResponse(
        Long id,
        List<String> deleteImageUrls
) {
    public static DeleteRecruitmentResponse of(Long id, List<String> deleteImageUrls) {
        return new DeleteRecruitmentResponse(id, deleteImageUrls);
    }
}

