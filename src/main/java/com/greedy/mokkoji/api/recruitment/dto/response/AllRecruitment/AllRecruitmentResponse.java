package com.greedy.mokkoji.api.recruitment.dto.response.AllRecruitment;

import com.greedy.mokkoji.api.club.dto.page.PageResponse;

import java.util.List;

//Todo: UI가 어떻게 나올지 몰라 필요한 것 같아 보이는 DTO 필드는 다 넣어뒀어요. 나중에 필요없는 거 빼야할 거 같아요.


public record AllRecruitmentResponse(
        List<RecruitmentPreviewResponse> recruitments,
        PageResponse page
) {
    public static AllRecruitmentResponse of(List<RecruitmentPreviewResponse> recruitments, PageResponse page) {
        return new AllRecruitmentResponse(recruitments, page);
    }
}

