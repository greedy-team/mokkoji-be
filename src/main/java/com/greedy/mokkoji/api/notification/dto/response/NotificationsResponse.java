package com.greedy.mokkoji.api.notification.dto.response;

import com.greedy.mokkoji.api.pagination.dto.PageResponse;

import java.util.List;

public record NotificationsResponse(
        List<NotificationResponse> notifications,
        PageResponse page
) {
    public static NotificationsResponse of(final List<NotificationResponse> notifications, final PageResponse page) {
        return new NotificationsResponse(notifications, page);
    }
}
