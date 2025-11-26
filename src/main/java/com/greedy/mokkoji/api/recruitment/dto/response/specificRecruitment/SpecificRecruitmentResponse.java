package com.greedy.mokkoji.api.recruitment.dto.response.specificRecruitment;

import com.greedy.mokkoji.enums.recruitment.RecruitStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "특정 모집글 상세 조회 응답")
public record SpecificRecruitmentResponse(
        @Schema(description = "모집글 ID", example = "1") Long id,
        @Schema(description = "모집글 제목", example = "신입부원 모집!") String title,
        @Schema(description = "동아리명", example = "그리디") String clubName,
        @Schema(description = "동아리 로고", example = "greedy_logo.jpg") String logo,
        @Schema(description = "동아리 ID", example = "1") Long clubId,
        @Schema(description = "모집글 내용", example = "세종대학교 개발 동아리에서 신입 회원을 모집합니다!") String content,
        @Schema(description = "모집 시작일", example = "2025-11-25T00:00:00") LocalDateTime recruitStart,
        @Schema(description = "모집 종료일", example = "2025-12-04T23:59:59") LocalDateTime recruitEnd,
        @Schema(description = "모집 상태", example = "OPEN") RecruitStatus status,
        @Schema(description = "생성일", example = "2025-11-20T10:00:00") LocalDateTime createdAt,
        @Schema(description = "이미지 URL 리스트") List<String> imageUrls,
        @Schema(description = "지원폼 링크", example = "https://forms.gle/abcdEFGH1234") String recruitForm,
        @Schema(description = "즐겨찾기 여부", example = "true") Boolean isFavorite,
        @Schema(description = "인스타그램 URL", example = "https://instagram.com/greedy_sejong") String instagramUrl,
        @Schema(description = "동아리 카테고리", example = "학술/교양") String category,
        @Schema(description = "상시 모집 여부", example = "false") boolean isAlwaysRecruiting
) {

    public static SpecificRecruitmentResponse of(
            Long id,
            String title,
            String clubName,
            String logo,
            Long clubId,
            String content,
            LocalDateTime recruitStart,
            LocalDateTime recruitEnd,
            RecruitStatus status,
            LocalDateTime createdAt,
            List<String> imageUrls,
            String recruitForm,
            Boolean isFavorite,
            String instagramUrl,
            String category,
            boolean isAlwaysRecruiting
    ) {
        return new SpecificRecruitmentResponse(
                id, title, clubName, logo, clubId, content, recruitStart, recruitEnd,
                status, createdAt, imageUrls, recruitForm,
                isFavorite, instagramUrl, category, isAlwaysRecruiting
        );
    }
}
