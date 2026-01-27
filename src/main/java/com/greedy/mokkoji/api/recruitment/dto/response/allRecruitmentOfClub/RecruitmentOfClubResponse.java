package com.greedy.mokkoji.api.recruitment.dto.response.allRecruitmentOfClub;

import com.greedy.mokkoji.enums.recruitment.RecruitStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record RecruitmentOfClubResponse(
        @Schema(example = "1") Long id,
        @Schema(example = "신입부원 모집!") String title,
        @Schema(example = "세종대학교 개발 동아리에서 신입 회원을 모집합니다!") String content,
        @Schema(example = "2025-11-25T15:00:00") LocalDateTime recruitStart,
        @Schema(example = "2025-12-04T23:59:59") LocalDateTime recruitEnd,
        @Schema(example = "OPEN") RecruitStatus status,
        @Schema(example = "2025-11-20T10:00:00") LocalDateTime createdAt,
        @Schema(example = "false") boolean isAlwaysRecruiting
) {
}
