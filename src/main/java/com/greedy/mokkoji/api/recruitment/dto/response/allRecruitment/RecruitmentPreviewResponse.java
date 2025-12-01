package com.greedy.mokkoji.api.recruitment.dto.response.allRecruitment;

import com.greedy.mokkoji.enums.recruitment.RecruitStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record RecruitmentPreviewResponse(
        ClubPreviewResponse club,
        @Schema(example = "1") Long id,
        @Schema(example = "신입부원 모집!") String title,
        @Schema(example = "2025-11-25T00:00:00") LocalDateTime recruitStart,
        @Schema(example = "2025-12-04T23:59:59") LocalDateTime recruitEnd,
        @Schema(example = "OPEN") RecruitStatus status,
        @Schema(example = "true") boolean isFavorite,
        @Schema(example = "false") boolean isAlwaysRecruiting
) {
}
