package com.greedy.mokkoji.api.club.dto.response.allClubs;

import java.time.LocalDateTime;

public record LatestRecruitmentInfo(
        Long id,
        LocalDateTime recruitStart,
        LocalDateTime recruitEnd,
        boolean isAlwaysRecruiting
) {
}
