package com.greedy.mokkoji.api.club.dto.response.allClubs;

import com.greedy.mokkoji.api.pagination.dto.PageResponse;

import java.util.List;

public record AllClubsResponse(
        List<ClubPreviewResponse> clubs,
        PageResponse page
) {
    public static AllClubsResponse of(
            List<ClubPreviewResponse> clubs,
            PageResponse page
    ) {
        return new AllClubsResponse(clubs, page);
    }
}
