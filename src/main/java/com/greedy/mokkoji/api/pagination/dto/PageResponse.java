package com.greedy.mokkoji.api.pagination.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
public record PageResponse(
        @Schema(example = "1") int page,
        @Schema(example = "10") int size,
        @Schema(example = "5") int totalPages,
        @Schema(example = "50") int totalElements
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
