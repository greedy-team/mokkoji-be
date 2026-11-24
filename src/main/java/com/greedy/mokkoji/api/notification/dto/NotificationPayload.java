package com.greedy.mokkoji.api.notification.dto;

import com.greedy.mokkoji.api.notification.event.NotificationEvent;

public record NotificationPayload(
        String type,
        String title,
        String content,
        Long targetId
) {

    public static NotificationPayload from(final NotificationEvent event) {
        return new NotificationPayload(
                event.type().name(),
                event.type().getTitle(),
                event.content(),
                event.targetId()
        );
    }
}
