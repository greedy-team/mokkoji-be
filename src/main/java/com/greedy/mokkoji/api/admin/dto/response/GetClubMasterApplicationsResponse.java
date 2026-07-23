package com.greedy.mokkoji.api.admin.dto.response;

import com.greedy.mokkoji.api.pagination.dto.PageResponse;
import lombok.Builder;

import java.util.List;

@Builder
public record GetClubMasterApplicationsResponse(
        List<ClubMasterApplicationPreviewResponse> applications,
        PageResponse pagination
) {
    public static GetClubMasterApplicationsResponse of(
            final List<ClubMasterApplicationPreviewResponse> applications,
            final PageResponse pagination
    ) {
        return GetClubMasterApplicationsResponse.builder()
                .applications(applications)
                .pagination(pagination)
                .build();
    }
}
