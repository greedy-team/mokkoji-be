package com.greedy.mokkoji.api.clubapplication.dto.response;

import com.greedy.mokkoji.db.club.entity.Club;
import com.greedy.mokkoji.enums.club.ClubAffiliation;
import com.greedy.mokkoji.enums.club.ClubCategory;
import com.greedy.mokkoji.enums.university.UniversityCode;
import io.swagger.v3.oas.annotations.media.Schema;

public record AdminClubResponse(
        @Schema(example = "1") Long clubId,
        @Schema(example = "그리디") String clubName,
        @Schema(example = "SEJONG") UniversityCode universityCode,
        @Schema(example = "세종대학교") String universityName,
        @Schema(example = "ACADEMIC_CULTURAL") ClubCategory category,
        @Schema(example = "CENTRAL_CLUB") ClubAffiliation affiliation,
        @Schema(example = "https://s3.amazonaws.com/.../logo.png") String logo,
        @Schema(example = "1") Long masterId,
        @Schema(example = "홍길동") String masterName
) {
    public static AdminClubResponse from(final Club club) {
        return new AdminClubResponse(
                club.getId(),
                club.getName(),
                club.getUniversity().getCode(),
                club.getUniversity().getName(),
                club.getClubCategory(),
                club.getClubAffiliation(),
                club.getLogo(),
                club.getMaster() != null ? club.getMaster().getId() : null,
                club.getMaster() != null ? club.getMaster().getName() : null
        );
    }
}
