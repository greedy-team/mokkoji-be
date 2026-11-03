package com.greedy.mokkoji.api.notification.service;

import com.greedy.mokkoji.api.notification.dto.response.NotificationResponse;
import com.greedy.mokkoji.api.notification.dto.response.NotificationsResponse;
import com.greedy.mokkoji.api.notification.dto.response.UnreadCountResponse;
import com.greedy.mokkoji.api.pagination.dto.PageResponse;
import com.greedy.mokkoji.common.exception.MokkojiException;
import com.greedy.mokkoji.db.notification.entity.Notification;
import com.greedy.mokkoji.db.notification.repository.NotificationRepository;
import com.greedy.mokkoji.db.user.entity.User;
import com.greedy.mokkoji.db.user.repository.UserRepository;
import com.greedy.mokkoji.enums.message.FailMessage;
import com.greedy.mokkoji.enums.notification.NotificationType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    @Transactional
    public Notification save(
            final Long userId,
            final NotificationType type,
            final String content,
            final Long targetId
    ) {
        final User user = getUserById(userId);

        final Notification notification = Notification.builder()
                .user(user)
                .type(type)
                .title(type.getTitle())
                .content(content)
                .targetId(targetId)
                .build();

        return notificationRepository.save(notification);
    }

    @Transactional(readOnly = true)
    public NotificationsResponse getNotifications(final Long userId, final Pageable pageable) {
        final Page<Notification> page = notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);

        final List<NotificationResponse> notifications = page.getContent()
                .stream()
                .map(NotificationResponse::from)
                .toList();

        return NotificationsResponse.of(
                notifications,
                PageResponse.of(page.getNumber() + 1, page.getSize(), page.getTotalPages(), (int) page.getTotalElements())
        );
    }

    @Transactional(readOnly = true)
    public UnreadCountResponse getUnreadCount(final Long userId) {
        return UnreadCountResponse.of(notificationRepository.countByUserIdAndIsReadFalse(userId));
    }

    @Transactional
    public void markAsRead(final Long userId, final Long notificationId) {
        final Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new MokkojiException(FailMessage.NOT_FOUND_NOTIFICATION));

        if (!notification.getUser().getId().equals(userId)) {
            throw new MokkojiException(FailMessage.FORBIDDEN_NOT_NOTIFICATION_OWNER);
        }

        notification.markAsRead();
    }

    @Transactional
    public void markAllAsRead(final Long userId) {
        notificationRepository.markAllAsReadByUserId(userId);
    }

    private User getUserById(final Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new MokkojiException(FailMessage.NOT_FOUND_USER));
    }
}
