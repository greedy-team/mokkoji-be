package com.greedy.mokkoji.api.recruitment.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "모집글 수정 요청")
public record UpdateRecruitmentRequest(
        @Schema(description = "제목", example = "신입부원 모집!") String title,
        @Schema(description = "이미지 리스트", example = "[\"image1.jpg\", \"image2.jpg\"]") List<String> imageNames,
        @Schema(description = "모집글 내용", example = "세종대학교 개발 동아리에서 신입 회원을 모집합니다!") String content,
        @Schema(description = "모집 시작일", example = "2025-11-25T15:00:00") LocalDateTime recruitStart,
        @Schema(description = "모집 종료일", example = "2025-12-04T23:59:59") LocalDateTime recruitEnd,
        @Schema(description = "지원폼 링크", example = "https://forms.gle/abcdEFGH1234") String recruitForm,
        @Schema(description = "상시 모집 여부", example = "false") boolean isAlwaysRecruiting
) {
}

