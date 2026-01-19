package com.greedy.mokkoji.api.club.dto.response.allClubs;

import com.greedy.mokkoji.api.pagination.dto.PageResponse;

import java.util.List;

public record AllClubsResponse(
        List<com.greedy.mokkoji.api.club.dto.response.allClubs.ClubPreviewResponse> clubs,
        PageResponse page
) {
    public static com.greedy.mokkoji.api.club.dto.response.allClubs.AllClubsResponse of(
            List<ClubPreviewResponse> clubs,
            PageResponse page
    ) {
        return new com.greedy.mokkoji.api.club.dto.response.allClubs.AllClubsResponse(clubs, page);
    }
}

