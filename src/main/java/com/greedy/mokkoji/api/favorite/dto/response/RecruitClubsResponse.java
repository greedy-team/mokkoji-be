package com.greedy.mokkoji.api.favorite.dto.response;

import java.time.LocalDateTime;

public record RecruitClubsResponse(
        Long clubId,
        String clubName,
        LocalDateTime recruitStart,
        LocalDateTime recruitEnd
) {
    public static RecruitClubsResponse of(Long clubId, String clubName, LocalDateTime recruitStart, LocalDateTime recruitEnd) {
        return new RecruitClubsResponse(clubId, clubName, recruitStart, recruitEnd);
    }
}
