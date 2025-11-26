package com.greedy.mokkoji.api.recruitment.dto.response.recentRecruitment;

import com.greedy.mokkoji.enums.recruitment.RecruitStatus;

import java.time.LocalDateTime;
import java.util.List;

public record RecentRecruitmentOfClubResponse(
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

    public static RecentRecruitmentOfClubResponse of(
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
        return new RecentRecruitmentOfClubResponse(
                id, title, clubName, logo, clubId, content, recruitStart, recruitEnd,
                status, createdAt, imageUrls, recruitForm,
                isFavorite, instagramUrl, category, isAlwaysRecruiting
        );
    }

}
