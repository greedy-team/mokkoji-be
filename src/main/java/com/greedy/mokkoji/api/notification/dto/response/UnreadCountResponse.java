package com.greedy.mokkoji.api.notification.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record UnreadCountResponse(
        @Schema(example = "3") long unreadCount
) {
    public static UnreadCountResponse of(final long unreadCount) {
        return new UnreadCountResponse(unreadCount);
    }
}
