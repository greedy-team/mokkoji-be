package com.greedy.mokkoji.api.clubapplication.dto.response;

import com.greedy.mokkoji.api.pagination.dto.PageResponse;

import java.util.List;

public record AdminClubApplicationsResponse(
        List<AdminClubApplicationResponse> applications,
        PageResponse page
) {
    public static AdminClubApplicationsResponse of(final List<AdminClubApplicationResponse> applications, final PageResponse page) {
        return new AdminClubApplicationsResponse(applications, page);
    }
}
