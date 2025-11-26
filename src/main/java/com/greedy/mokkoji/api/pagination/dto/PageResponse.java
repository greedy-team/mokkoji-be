package com.greedy.mokkoji.api.pagination.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "페이지네이션 정보")
public record PageResponse(
        @Schema(description = "현재 페이지 번호", example = "0") int page,
        @Schema(description = "페이지 크기", example = "10") int size,
        @Schema(description = "전체 페이지 수", example = "5") int totalPages,
        @Schema(description = "전체 요소 수", example = "50") int totalElements
) {
    public static PageResponse of(final int page, final int size, final int totalPages, final int totalElements) {
        return PageResponse.builder()
                .page(page)
                .size(size)
                .totalPages(totalPages)
                .totalElements(totalElements)
                .build();
    }
}
