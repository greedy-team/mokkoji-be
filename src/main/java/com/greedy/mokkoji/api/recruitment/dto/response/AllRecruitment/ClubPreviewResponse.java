package com.greedy.mokkoji.api.recruitment.dto.response.AllRecruitment;

import com.greedy.mokkoji.enums.club.ClubAffiliation;
import com.greedy.mokkoji.enums.club.ClubCategory;
import lombok.Builder;

@Builder
public record ClubPreviewResponse(
        Long id,
        String name,
        String description,
        ClubCategory clubCategory,
        ClubAffiliation clubAffiliation,
        String logo
) {
}
