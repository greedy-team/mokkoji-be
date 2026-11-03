package com.greedy.mokkoji.notification.service;

import com.greedy.mokkoji.api.notification.dto.response.NotificationsResponse;
import com.greedy.mokkoji.api.notification.dto.response.UnreadCountResponse;
import com.greedy.mokkoji.api.notification.service.NotificationService;
import com.greedy.mokkoji.common.exception.MokkojiException;
import com.greedy.mokkoji.common.fixture.Fixture;
import com.greedy.mokkoji.db.notification.entity.Notification;
import com.greedy.mokkoji.db.notification.repository.NotificationRepository;
import com.greedy.mokkoji.db.university.entity.University;
import com.greedy.mokkoji.db.user.entity.User;
import com.greedy.mokkoji.db.user.repository.UserRepository;
import com.greedy.mokkoji.enums.message.FailMessage;
import com.greedy.mokkoji.enums.notification.NotificationType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
@DisplayName("알림 서비스 테스트")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class NotificationServiceTest {

    @InjectMocks
    NotificationService notificationService;

    @Mock
    NotificationRepository notificationRepository;

    @Mock
    UserRepository userRepository;

    private User userWithId(final Long id) {
        final University university = Fixture.createUniversity();
        final User user = Fixture.createUser(university);
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    @Test
    @DisplayName("알림을 저장하면 type의 title이 함께 저장된다.")
    void saveNotificationWithTypeTitle() {
        final User user = userWithId(1L);
        BDDMockito.given(userRepository.findById(1L)).willReturn(Optional.of(user));
        BDDMockito.given(notificationRepository.save(any(Notification.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        final Notification saved = notificationService.save(
                1L,
                NotificationType.CLUB_APPLICATION_APPROVED,
                "'편지' 동아리가 생성되었습니다",
                352L
        );

        assertThat(saved.getTitle()).isEqualTo("동아리 생성이 승인되었습니다");
        assertThat(saved.getContent()).isEqualTo("'편지' 동아리가 생성되었습니다");
        assertThat(saved.getTargetId()).isEqualTo(352L);
        assertThat(saved.isRead()).isFalse();
    }

    @Test
    @DisplayName("존재하지 않는 유저에게 알림을 저장하면 예외가 발생한다.")
    void saveNotificationUserNotFound() {
        BDDMockito.given(userRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> notificationService.save(
                999L, NotificationType.CLUB_APPLICATION_APPROVED, "내용", null))
                .isInstanceOf(MokkojiException.class)
                .hasMessage(FailMessage.NOT_FOUND_USER.getMessage());
    }

    @Test
    @DisplayName("내 알림 목록을 최신순으로 조회한다.")
    void getNotifications() {
        final User user = userWithId(1L);
        final Notification notification = Fixture.createNotification(user);
        ReflectionTestUtils.setField(notification, "id", 10L);

        final Pageable pageable = PageRequest.of(0, 10);
        BDDMockito.given(notificationRepository.findByUserIdOrderByCreatedAtDesc(1L, pageable))
                .willReturn(new PageImpl<>(List.of(notification), pageable, 1));

        final NotificationsResponse response = notificationService.getNotifications(1L, pageable);

        assertThat(response.notifications()).hasSize(1);
        assertThat(response.notifications().get(0).title()).isEqualTo("동아리 생성이 승인되었습니다");
        assertThat(response.page().page()).isEqualTo(1);
        assertThat(response.page().totalElements()).isEqualTo(1);
    }

    @Test
    @DisplayName("안 읽은 알림 개수를 조회한다.")
    void getUnreadCount() {
        BDDMockito.given(notificationRepository.countByUserIdAndIsReadFalse(1L)).willReturn(3L);

        final UnreadCountResponse response = notificationService.getUnreadCount(1L);

        assertThat(response.unreadCount()).isEqualTo(3L);
    }

    @Test
    @DisplayName("알림을 읽음 처리한다.")
    void markAsRead() {
        final User user = userWithId(1L);
        final Notification notification = Fixture.createNotification(user);
        BDDMockito.given(notificationRepository.findById(10L)).willReturn(Optional.of(notification));

        notificationService.markAsRead(1L, 10L);

        assertThat(notification.isRead()).isTrue();
    }

    @Test
    @DisplayName("본인의 알림이 아니면 읽음 처리 시 예외가 발생한다.")
    void markAsReadForbidden() {
        final User owner = userWithId(1L);
        final Notification notification = Fixture.createNotification(owner);
        BDDMockito.given(notificationRepository.findById(10L)).willReturn(Optional.of(notification));

        assertThatThrownBy(() -> notificationService.markAsRead(2L, 10L))
                .isInstanceOf(MokkojiException.class)
                .hasMessage(FailMessage.FORBIDDEN_NOT_NOTIFICATION_OWNER.getMessage());

        assertThat(notification.isRead()).isFalse();
    }

    @Test
    @DisplayName("존재하지 않는 알림을 읽음 처리하면 예외가 발생한다.")
    void markAsReadNotificationNotFound() {
        BDDMockito.given(notificationRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> notificationService.markAsRead(1L, 999L))
                .isInstanceOf(MokkojiException.class)
                .hasMessage(FailMessage.NOT_FOUND_NOTIFICATION.getMessage());
    }

    @Test
    @DisplayName("모두 읽음 처리를 요청하면 리포지토리에 위임한다.")
    void markAllAsRead() {
        notificationService.markAllAsRead(1L);

        BDDMockito.then(notificationRepository).should().markAllAsReadByUserId(1L);
    }
}
