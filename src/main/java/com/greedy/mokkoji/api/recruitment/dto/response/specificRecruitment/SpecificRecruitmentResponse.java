package com.greedy.mokkoji.api.recruitment.dto.response.specificRecruitment;

import com.greedy.mokkoji.enums.recruitment.RecruitStatus;
import java.time.LocalDateTime;
import java.util.List;

public record SpecificRecruitmentResponse(
    Long id,
    String title,
    String clubName,
    String clubLogo,
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
    String category
) {

    public static SpecificRecruitmentResponse of(
        Long id,
        String title,
        String clubName,
        String clubLogo,
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
        String category
    ) {
        return new SpecificRecruitmentResponse(
            id, title, clubName, clubLogo, clubId, content, recruitStart, recruitEnd,
            status, createdAt, imageUrls, recruitForm,
            isFavorite, instagramUrl, category
        );
    }
}
