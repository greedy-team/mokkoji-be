package com.greedy.mokkoji.api.recruitment.dto;

import com.greedy.mokkoji.db.recruitment.entity.Recruitment;
import lombok.Builder;

import java.util.List;

@Builder
public record RecruitmentCreateResponse (
        Recruitment recruitment,
        List<String> images
){
}
