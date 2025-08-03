package com.greedy.mokkoji.api.club.dto.request;

import com.greedy.mokkoji.enums.club.ClubAffiliation;
import com.greedy.mokkoji.enums.club.ClubCategory;

public record ClubUpdateRequest(
        String name,
        ClubCategory category,
        ClubAffiliation affiliation,
        String description,
        String clubMasterStudentId,
        String logo,
        String instagram
) {
}
