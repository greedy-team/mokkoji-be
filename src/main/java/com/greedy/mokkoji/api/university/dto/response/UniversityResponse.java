package com.greedy.mokkoji.api.university.dto.response;

import com.greedy.mokkoji.db.university.entity.University;
import io.swagger.v3.oas.annotations.media.Schema;

public record UniversityResponse(
        @Schema(example = "1") Long id,
        @Schema(example = "세종대학교") String name,
        @Schema(example = "sejong") String code,
        @Schema(example = "https://mokkoji-app-data.s3.ap-northeast-2.amazonaws.com/university-logo/2/sejong.jpg") String logo
) {
    public static UniversityResponse from(final University university) {
        return new UniversityResponse(
                university.getId(),
                university.getName(),
                university.getCode().name().toLowerCase(),
                university.getLogo()
        );
    }
}
