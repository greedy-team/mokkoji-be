package com.greedy.mokkoji.api.recruitment.dto.response.allRecruitmentOfClub;

import com.greedy.mokkoji.enums.recruitment.RecruitStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
@Schema(description = "동아리별 모집글 응답")
public record RecruitmentOfClubResponse(
        @Schema(description = "모집글 ID", example = "1") Long id,
        @Schema(description = "모집글 제목", example = "신입부원 모집!") String title,
        @Schema(description = "모집글 내용", example = "세종대학교 개발 동아리에서 신입 회원을 모집합니다!") String content,
        @Schema(description = "모집 시작일", example = "2025-11-25T15:00:00") LocalDateTime recruitStart,
        @Schema(description = "모집 종료일", example = "2025-12-04T23:59:59") LocalDateTime recruitEnd,
        @Schema(description = "모집 상태", example = "OPEN") RecruitStatus status,
        @Schema(description = "생성일", example = "2025-11-20T10:00:00") LocalDateTime createdAt,
        @Schema(description = "첫 번째 이미지 Presigned URL", example = "https://s3.amazonaws.com/bucket/recruitment-image/1/2/uuid.jpg") String firstImage,
        @Schema(description = "상시 모집 여부", example = "false") boolean isAlwaysRecruiting
) {
}
