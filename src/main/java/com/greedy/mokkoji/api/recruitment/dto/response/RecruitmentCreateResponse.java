package com.greedy.mokkoji.api.recruitment.dto.response;

import lombok.Builder;

import java.util.List;

@Builder
public record RecruitmentCreateResponse(
        Long id,
        List<String> presignedPutUrls
) {
    public static RecruitmentCreateResponse of(Long id, List<String> presignedPutUrls) {
        return new RecruitmentCreateResponse(id, presignedPutUrls);
    }
}

