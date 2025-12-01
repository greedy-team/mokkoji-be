package com.greedy.mokkoji.api.club.dto.response;

import com.greedy.mokkoji.api.pagination.dto.PageResponse;
import lombok.Builder;

import java.util.List;

@Builder
public record ClubsPaginationResponse(
        List<ClubResponse> clubs,
        PageResponse pagination
) {
    public static ClubsPaginationResponse of(final List<ClubResponse> clubResponses, final PageResponse pageResponse) {
        return ClubsPaginationResponse.builder()
                .clubs(clubResponses)
                .pagination(pageResponse)
                .build();
    }
}
