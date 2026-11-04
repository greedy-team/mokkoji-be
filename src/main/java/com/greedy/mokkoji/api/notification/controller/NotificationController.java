package com.greedy.mokkoji.api.notification.controller;

import com.greedy.mokkoji.api.auth.controller.argumentResolver.AuthCredential;
import com.greedy.mokkoji.api.auth.controller.argumentResolver.Authentication;
import com.greedy.mokkoji.api.notification.dto.response.NotificationsResponse;
import com.greedy.mokkoji.api.notification.dto.response.UnreadCountResponse;
import com.greedy.mokkoji.api.notification.service.NotificationService;
import com.greedy.mokkoji.api.notification.sse.NotificationSseService;
import com.greedy.mokkoji.common.response.APISuccessResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequiredArgsConstructor
@RequestMapping("${api.prefix}/notifications")
public class NotificationController implements NotificationControllerSwagger {

    private final NotificationService notificationService;
    private final NotificationSseService notificationSseService;

    @GetMapping
    public ResponseEntity<APISuccessResponse<NotificationsResponse>> getNotifications(
            @Authentication final AuthCredential authCredential,
            @RequestParam(value = "page") final int page,
            @RequestParam(value = "size") final int size
    ) {
        final Pageable pageable = PageRequest.of(page - 1, size);
        return APISuccessResponse.of(
                HttpStatus.OK,
                notificationService.getNotifications(authCredential.accountId(), pageable)
        );
    }

    @GetMapping("/unread-count")
    public ResponseEntity<APISuccessResponse<UnreadCountResponse>> getUnreadCount(
            @Authentication final AuthCredential authCredential
    ) {
        return APISuccessResponse.of(
                HttpStatus.OK,
                notificationService.getUnreadCount(authCredential.accountId())
        );
    }

    @PatchMapping("/{notificationId}/read")
    public ResponseEntity<APISuccessResponse<Void>> markAsRead(
            @Authentication final AuthCredential authCredential,
            @PathVariable final Long notificationId
    ) {
        notificationService.markAsRead(authCredential.accountId(), notificationId);
        return APISuccessResponse.of(HttpStatus.OK, null);
    }

    @PatchMapping("/read-all")
    public ResponseEntity<APISuccessResponse<Void>> markAllAsRead(
            @Authentication final AuthCredential authCredential
    ) {
        notificationService.markAllAsRead(authCredential.accountId());
        return APISuccessResponse.of(HttpStatus.OK, null);
    }

    @GetMapping(value = "/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe(@Authentication final AuthCredential authCredential) {
        return notificationSseService.subscribe(authCredential.accountId());
    }
}
