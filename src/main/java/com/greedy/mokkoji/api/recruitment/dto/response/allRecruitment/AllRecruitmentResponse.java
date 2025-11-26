package com.greedy.mokkoji.api.recruitment.dto.response.allRecruitment;

import com.greedy.mokkoji.api.pagination.dto.PageResponse;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

//Todo: UI가 어떻게 나올지 몰라 필요한 것 같아 보이는 DTO 필드는 다 넣어뒀어요. 나중에 필요없는 거 빼야할 거 같아요.

@Schema(description = "전체 모집글 목록 응답")
public record AllRecruitmentResponse(
        @Schema(description = "모집글 미리보기 목록") List<RecruitmentPreviewResponse> recruitments,
        @Schema(description = "페이지 정보") PageResponse page
) {
    public static AllRecruitmentResponse of(List<RecruitmentPreviewResponse> recruitments, PageResponse page) {
        return new AllRecruitmentResponse(recruitments, page);
    }
}

