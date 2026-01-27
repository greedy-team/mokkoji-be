package com.greedy.mokkoji.api.club.dto.response.allClubs;

import com.greedy.mokkoji.enums.recruitment.RecruitStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record RecruitmentPreviewResponse(
        @Schema(example = "1") Long id,
        @Schema(example = "2025-11-25T00:00:00") LocalDateTime recruitStart,
        @Schema(example = "2025-12-04T23:59:59") LocalDateTime recruitEnd,
        @Schema(example = "OPEN") RecruitStatus recruitStatus
) {
}
