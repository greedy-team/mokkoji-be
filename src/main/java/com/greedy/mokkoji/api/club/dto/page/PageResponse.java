package com.greedy.mokkoji.api.club.dto.page;

import lombok.Builder;

@Builder
public record PageResponse(
        int page,
        int size,
        int totalPages,
        int totalElements
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
