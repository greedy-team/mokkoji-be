package com.greedy.mokkoji.api.club.dto.club;

import com.greedy.mokkoji.api.club.dto.page.PageResponse;
import lombok.Builder;

import java.util.List;

@Builder
public record ClubSearchResponse(
        List<ClubResponse> clubs,
        PageResponse pagination
) {
    public static ClubSearchResponse of(final List<ClubResponse> clubResponses, final PageResponse pageResponse) {
        return ClubSearchResponse.builder()
                .clubs(clubResponses)
                .pagination(pageResponse)
                .build();
    }
}
