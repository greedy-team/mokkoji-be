package com.greedy.mokkoji.api.club.dto.response;

import com.greedy.mokkoji.api.pagination.dto.PageResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.util.List;

@Builder
@Schema(description = "동아리 목록 페이지네이션 응답")
public record ClubsPaginationResponse(
        @Schema(description = "동아리 목록") List<ClubResponse> clubs,
        @Schema(description = "페이지 정보") PageResponse pagination
) {
    public static ClubsPaginationResponse of(final List<ClubResponse> clubResponses, final PageResponse pageResponse) {
        return ClubsPaginationResponse.builder()
                .clubs(clubResponses)
                .pagination(pageResponse)
                .build();
    }
}
