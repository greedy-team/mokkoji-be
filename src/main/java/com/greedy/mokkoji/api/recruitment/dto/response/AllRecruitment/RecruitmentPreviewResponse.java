package com.greedy.mokkoji.api.recruitment.dto.response.AllRecruitment;

import com.greedy.mokkoji.enums.recruitment.RecruitStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
@Schema(description = "모집글 미리보기 응답")
public record RecruitmentPreviewResponse(
        @Schema(description = "동아리 미리보기 정보") ClubPreviewResponse club,
        @Schema(description = "모집글 ID", example = "1") Long id,
        @Schema(description = "모집글 제목", example = "신입부원 모집!") String title,
        @Schema(description = "모집 시작일", example = "2025-11-25T15:00:00") LocalDateTime recruitStart,
        @Schema(description = "모집 종료일", example = "2025-12-04T23:59:59") LocalDateTime recruitEnd,
        @Schema(description = "모집 상태", example = "OPEN") RecruitStatus status,
        @Schema(description = "즐겨찾기 여부", example = "true") boolean isFavorite,
        @Schema(description = "상시 모집 여부", example = "false") boolean isAlwaysRecruiting
) {
}
