package com.greedy.mokkoji.api.recruitment.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

public record UpdateRecruitmentRequest(
        @Schema(example = "신입부원 모집!") String title,
        @Schema(example = "[\"image1.jpg\", \"image2.jpg\"]") List<String> imageNames,
        @Schema(example = "세종대학교 개발 동아리에서 신입 회원을 모집합니다!") String content,
        @Schema(example = "2025-11-25T00:00:00") LocalDateTime recruitStart,
        @Schema(example = "2025-12-04T23:59:59") LocalDateTime recruitEnd,
        @Schema(example = "https://forms.gle/abcdEFGH1234") String recruitForm,
        @Schema(example = "false") boolean isAlwaysRecruiting
) {
}

