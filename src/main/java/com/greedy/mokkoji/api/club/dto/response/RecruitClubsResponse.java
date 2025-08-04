package com.greedy.mokkoji.api.club.dto.response;

import java.time.LocalDateTime;

public record RecruitClubsResponse (
        String clubName,
        LocalDateTime recruitStart,
        LocalDateTime recruitEnd
){
    public static RecruitClubsResponse of(String clubName, LocalDateTime recruitStart, LocalDateTime recruitEnd) {
        return new RecruitClubsResponse(clubName, recruitStart, recruitEnd);
    }
}
