package com.greedy.mokkoji.api.recruitment.dto.response.specificRecruitment;

import com.greedy.mokkoji.enums.club.ClubCategory;
import com.greedy.mokkoji.enums.recruitment.RecruitStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

public record SpecificRecruitmentResponse(
        @Schema(example = "1") Long id,
        @Schema(example = "신입부원 모집!") String title,
        @Schema(example = "그리디") String clubName,
        @Schema(example = "https://mokkoji-app-data.s3.ap-northeast-2.amazonaws.com/club-logo/1/greedy_{UUID}.jpg") String logo,
        @Schema(example = "1") Long clubId,
        @Schema(example = "세종대학교 개발 동아리에서 신입 회원을 모집합니다!") String content,
        @Schema(example = "2025-11-25T00:00:00") LocalDateTime recruitStart,
        @Schema(example = "2025-12-04T23:59:59") LocalDateTime recruitEnd,
        @Schema(example = "OPEN") RecruitStatus status,
        @Schema(example = "2025-11-20T10:00:00") LocalDateTime createdAt,
        @Schema(example = "[\"https://mokkoji-app-data.s3.ap-northeast-2.amazonaws.com/recruitment-image/1/2/{UUID}.jpg\"]") List<String> imageUrls,
        @Schema(example = "https://forms.gle/abcdEFGH1234") String recruitForm,
        @Schema(example = "true") Boolean isFavorite,
        @Schema(example = "https://instagram.com/greedy_sejong") String instagramUrl,
        @Schema(example = "ACADEMIC_CULTURAL") ClubCategory category,
        @Schema(example = "false") boolean isAlwaysRecruiting
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
            ClubCategory category,
            boolean isAlwaysRecruiting
    ) {
        return new SpecificRecruitmentResponse(
                id, title, clubName, logo, clubId, content, recruitStart, recruitEnd,
                status, createdAt, imageUrls, recruitForm,
                isFavorite, instagramUrl, category, isAlwaysRecruiting
        );
    }
}
