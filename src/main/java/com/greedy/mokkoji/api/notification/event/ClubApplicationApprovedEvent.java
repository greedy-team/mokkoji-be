package com.greedy.mokkoji.api.notification.event;

import com.greedy.mokkoji.enums.notification.NotificationType;

public record ClubApplicationApprovedEvent(
        Long userId,
        Long clubId,
        String clubName
) implements NotificationEvent {

    @Override
    public NotificationType type() {
        return NotificationType.CLUB_APPLICATION_APPROVED;
    }

    @Override
    public String content() {
        return type().formatContent(clubName);
    }

    @Override
    public Long targetId() {
        return clubId;
    }

    @Override
    public boolean roleChanged() {
        return true;
    }
}
