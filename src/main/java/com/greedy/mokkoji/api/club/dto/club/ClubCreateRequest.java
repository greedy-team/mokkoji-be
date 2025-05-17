package com.greedy.mokkoji.api.club.dto.club;

import com.greedy.mokkoji.enums.club.ClubAffiliation;
import com.greedy.mokkoji.enums.club.ClubCategory;

public record ClubCreateRequest(
        String name,
        ClubCategory category,
        ClubAffiliation affiliation,
        String clubMasterStudentId
) {
}
