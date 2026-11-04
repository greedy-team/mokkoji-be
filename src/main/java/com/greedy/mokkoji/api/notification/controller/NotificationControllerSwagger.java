package com.greedy.mokkoji.api.notification.controller;

import com.greedy.mokkoji.api.auth.controller.argumentResolver.AuthCredential;
import com.greedy.mokkoji.api.notification.dto.response.NotificationsResponse;
import com.greedy.mokkoji.api.notification.dto.response.UnreadCountResponse;
import com.greedy.mokkoji.common.response.APISuccessResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Tag(name = "Notification Controller", description = "알림 관련 API")
public interface NotificationControllerSwagger {

    @Operation(
            summary = "내 알림 목록 조회 API",
            description = "최신순으로 조회",
            security = {@SecurityRequirement(name = "JWT")}
    )
    @ApiResponse(responseCode = "200", description = "조회 성공")
    ResponseEntity<APISuccessResponse<NotificationsResponse>> getNotifications(
            @Parameter(hidden = true) AuthCredential authCredential,
            @Parameter(name = "page", description = "페이지 번호(1부터)") int page,
            @Parameter(name = "size", description = "페이지 크기") int size
    );

    @Operation(
            summary = "안 읽은 알림 개수 조회 API",
            description = "종 아이콘의 badge 숫자에 사용",
            security = {@SecurityRequirement(name = "JWT")}
    )
    @ApiResponse(responseCode = "200", description = "조회 성공")
    ResponseEntity<APISuccessResponse<UnreadCountResponse>> getUnreadCount(
            @Parameter(hidden = true) AuthCredential authCredential
    );

    @Operation(
            summary = "알림 읽음 처리 API",
            security = {@SecurityRequirement(name = "JWT")}
    )
    @ApiResponse(responseCode = "200", description = "읽음 처리 성공")
    ResponseEntity<APISuccessResponse<Void>> markAsRead(
            @Parameter(hidden = true) AuthCredential authCredential,
            @Parameter(name = "notificationId", description = "알림 id") Long notificationId
    );

    @Operation(
            summary = "알림 모두 읽음 처리 API",
            security = {@SecurityRequirement(name = "JWT")}
    )
    @ApiResponse(responseCode = "200", description = "읽음 처리 성공")
    ResponseEntity<APISuccessResponse<Void>> markAllAsRead(
            @Parameter(hidden = true) AuthCredential authCredential
    );

    @Operation(
            summary = "알림 SSE 구독 API",
            description = "연결 직후 connect 이벤트를 한 번 보낸다. 이후 notification, role-changed 이벤트를 받는다.",
            security = {@SecurityRequirement(name = "JWT")}
    )
    @ApiResponse(responseCode = "200", description = "구독 성공 (text/event-stream)")
    SseEmitter subscribe(@Parameter(hidden = true) AuthCredential authCredential);
}
