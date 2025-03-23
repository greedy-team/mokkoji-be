package com.greedy.mokkoji.db.club.dto;

import com.greedy.mokkoji.enums.club.ClubAffiliation;
import com.greedy.mokkoji.enums.club.ClubCategory;
import com.querydsl.core.annotations.QueryProjection;

import java.time.LocalDateTime;

public record ClubRecruitmentDto(
        Long id,
        String name,
        ClubCategory clubCategory,
        ClubAffiliation clubAffiliation,
        String description,
        String logo,
        String instagram,
        LocalDateTime recruitStart,
        LocalDateTime recruitEnd
) {

    @QueryProjection
    public ClubRecruitmentDto {
    }
}
