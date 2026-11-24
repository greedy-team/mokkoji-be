package com.greedy.mokkoji.api.notification.event;

import com.greedy.mokkoji.enums.notification.NotificationType;

public record ClubDeletedEvent(
        Long userId,
        String clubName
) implements NotificationEvent {

    @Override
    public NotificationType type() {
        return NotificationType.CLUB_DELETED;
    }

    @Override
    public String content() {
        return type().formatContent(clubName);
    }

    @Override
    public Long targetId() {
        return null;
    }

    @Override
    public boolean roleChanged() {
        return true;
    }
}
