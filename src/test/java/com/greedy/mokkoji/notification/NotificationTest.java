package com.greedy.mokkoji.notification;

import com.greedy.mokkoji.api.test.notification.service.NotificationChannel;
import com.greedy.mokkoji.api.test.notification.service.NotificationService;
import com.greedy.mokkoji.db.club.entity.Club;
import com.greedy.mokkoji.db.favorite.entity.Favorite;
import com.greedy.mokkoji.db.favorite.repository.FavoriteRepository;
import com.greedy.mokkoji.db.recruitment.entity.Recruitment;
import com.greedy.mokkoji.db.user.entity.User;
import com.greedy.mokkoji.enums.ClubAffiliation;
import com.greedy.mokkoji.enums.ClubCategory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
@DisplayName("알림 서비스 테스트")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
public class NotificationTest {
    @InjectMocks
    NotificationService notificationService;

    @Mock
    FavoriteRepository favoriteRepository;

    @Mock
    NotificationChannel notificationChannel;

    @Test
    @DisplayName("사용자들에게 알림을 보낼 수 있다")
    void sendNotifications() {
        // given
        final User user1 = User.builder()
                .name("사용자 이름")
                .email("test1@test.com")
                .grade(4)
                .department("사용자 학과")
                .studentId("1111111")
                .build();

        final User user2 = User.builder()
                .name("사용자 이름")
                .email("test2@test.com")
                .grade(3)
                .department("사용자 학과")
                .studentId("222222")
                .build();


        final Club club = Club.builder()
                .name("동아리 이름")
                .clubAffiliation(ClubAffiliation.CENTRAL_CLUB)
                .clubCategory(ClubCategory.ETC)
                .logo("동아리 로고")
                .description("동아리 설명")
                .instagram("동아리 인스타 링크")
                .build();

        final Recruitment recruitment = Recruitment.builder()
                .club(club)
                .content("동아리 소개글")
                .recruitStart(LocalDateTime.now())
                .recruitEnd(LocalDateTime.now().plusDays(10))
                .build();

        final Favorite favorite1 = Favorite.builder()
                .user(user1)
                .club(club)
                .build();

        final Favorite favorite2 = Favorite.builder()
                .user(user2)
                .club(club)
                .build();

        BDDMockito.given(favoriteRepository.findByClubIdWithFetchJoin(any()))
                .willReturn(List.of(favorite1, favorite2));

        BDDMockito.doNothing().when(notificationChannel)
                .sendNotification(any(), any(), any(), any());

        // when
        notificationService.sendNotification(club, recruitment);

        // then
        BDDMockito.verify(favoriteRepository, times(1))
                .findByClubIdWithFetchJoin(any());

        BDDMockito.verify(notificationChannel, times(1))
                .sendNotification(any(), any(), any(), any());

    }
}
