package com.greedy.mokkoji.api.clubapplication.dto.response;

import java.util.List;

public record ClubApplicationsResponse(
        List<ClubApplicationResponse> clubApplications
) {
    public static ClubApplicationsResponse of(final List<ClubApplicationResponse> clubApplications) {
        return new ClubApplicationsResponse(clubApplications);
    }
}
