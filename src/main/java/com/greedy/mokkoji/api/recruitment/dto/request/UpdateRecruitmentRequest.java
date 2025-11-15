package com.greedy.mokkoji.api.recruitment.dto.request;

import java.time.LocalDateTime;
import java.util.List;

public record UpdateRecruitmentRequest(
        String title,
        List<String> deleteImageKeys,
        List<String> newImageNames,
        String content,
        LocalDateTime recruitStart,
        LocalDateTime recruitEnd,
        String recruitForm,
        boolean isAlwaysRecruiting
) {
}

