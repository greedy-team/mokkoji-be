package com.greedy.mokkoji.api.notification.event;

import com.greedy.mokkoji.enums.notification.NotificationType;

public record ClubMasterRejectedEvent(
        Long userId,
        String clubName,
        String rejectReason
) implements NotificationEvent {

    @Override
    public NotificationType type() {
        return NotificationType.CLUB_MASTER_REJECTED;
    }

    @Override
    public String content() {
        return type().formatContent(clubName, rejectReason);
    }

    @Override
    public Long targetId() {
        return null;
    }
}
