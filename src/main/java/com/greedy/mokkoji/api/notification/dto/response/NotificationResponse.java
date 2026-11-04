package com.greedy.mokkoji.api.notification.dto.response;

import com.greedy.mokkoji.db.notification.entity.Notification;
import com.greedy.mokkoji.enums.notification.NotificationType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

public record NotificationResponse(
        @Schema(example = "12") Long id,
        @Schema(example = "CLUB_APPLICATION_APPROVED") NotificationType type,
        @Schema(example = "동아리 생성이 승인되었습니다") String title,
        @Schema(example = "'편지' 동아리가 생성되었습니다") String content,
        @Schema(example = "352") Long targetId,
        @Schema(example = "false") boolean isRead,
        @Schema(example = "2026-11-03T14:02:11") LocalDateTime createdAt
) {
    public static NotificationResponse from(final Notification notification) {
        return new NotificationResponse(
                notification.getId(),
                notification.getType(),
                notification.getTitle(),
                notification.getContent(),
                notification.getTargetId(),
                notification.isRead(),
                notification.getCreatedAt()
        );
    }
}
