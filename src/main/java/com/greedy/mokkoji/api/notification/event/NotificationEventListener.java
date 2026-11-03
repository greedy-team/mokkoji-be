package com.greedy.mokkoji.api.notification.event;

import com.greedy.mokkoji.api.notification.service.NotificationService;
import com.greedy.mokkoji.api.notification.sse.NotificationSseService;
import com.greedy.mokkoji.db.notification.entity.Notification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventListener {

    private static final String EVENT_NOTIFICATION = "notification";
    private static final String EVENT_ROLE_CHANGED = "role-changed";

    private final NotificationService notificationService;
    private final NotificationSseService notificationSseService;

    @EventListener
    @Transactional
    public void saveNotification(final NotificationEvent event) {
        final Notification notification = notificationService.save(
                event.userId(),
                event.type(),
                event.content(),
                event.targetId()
        );

        log.info("알림 저장 : notificationId={}, userId={}, type={}",
                notification.getId(), event.userId(), event.type());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void sendNotification(final NotificationEvent event) {
        notificationSseService.send(
                event.userId(),
                EVENT_NOTIFICATION,
                NotificationPayload.of(event)
        );

        if (event.roleChanged()) {
            notificationSseService.send(event.userId(), EVENT_ROLE_CHANGED, "role-changed");
        }
    }

    private record NotificationPayload(String type, String title, String content, Long targetId) {
        private static NotificationPayload of(final NotificationEvent event) {
            return new NotificationPayload(
                    event.type().name(),
                    event.type().getTitle(),
                    event.content(),
                    event.targetId()
            );
        }
    }
}
