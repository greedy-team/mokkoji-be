package com.greedy.mokkoji.api.notification.event;

import com.greedy.mokkoji.enums.notification.NotificationType;

public interface NotificationEvent {

    Long userId();

    NotificationType type();

    String content();

    Long targetId();

    default boolean roleChanged() {
        return false;
    }
}
