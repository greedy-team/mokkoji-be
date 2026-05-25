package com.greedy.mokkoji.api.club.dto.response;

import com.greedy.mokkoji.api.pagination.dto.PageResponse;

import java.util.List;

public record AdminClubsResponse(
        List<AdminClubResponse> clubs,
        PageResponse page
) {
    public static AdminClubsResponse of(final List<AdminClubResponse> clubs, final PageResponse page) {
        return new AdminClubsResponse(clubs, page);
    }
}
