package com.greedy.mokkoji.notification;

import com.greedy.mokkoji.api.email.service.RecruitmentMailPayload;
import com.greedy.mokkoji.api.email.service.RecruitmentNotificationChannel;
import com.greedy.mokkoji.api.email.service.EmailService;
import com.greedy.mokkoji.db.club.entity.Club;
import com.greedy.mokkoji.db.favorite.entity.Favorite;
import com.greedy.mokkoji.db.favorite.repository.FavoriteRepository;
import com.greedy.mokkoji.db.recruitment.entity.Recruitment;
import com.greedy.mokkoji.db.user.entity.User;
import com.greedy.mokkoji.enums.club.ClubAffiliation;
import com.greedy.mokkoji.enums.club.ClubCategory;
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
    EmailService emailService;

    @Mock
    FavoriteRepository favoriteRepository;

    @Mock
    RecruitmentNotificationChannel recruitmentNotificationChannel;

    @Test
    @DisplayName("사용자들에게 알림을 보낼 수 있다")
    void sendNotifications() {
        // given
        final User user1 = User.builder()
                .name("사용자 이름")
                .email("test1@test.com")
                .isEmailOn(true)
                .build();

        final User user2 = User.builder()
                .name("사용자 이름")
                .email("test2@test.com")
                .isEmailOn(true)
                .build();

        final Club club = Club.builder()
                .name("동아리 이름")
                .clubAffiliation(ClubAffiliation.CENTRAL_CLUB)
                .clubCategory(ClubCategory.CULTURAL_ART)
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

        BDDMockito.doNothing().when(recruitmentNotificationChannel)
                .sendBatchNotification(any());

        // when
        emailService.sendBatchRecruitmentNotifications(List.of(recruitment));

        // then
        BDDMockito.verify(favoriteRepository, times(1))
                .findByClubIdWithFetchJoin(any());

        BDDMockito.verify(recruitmentNotificationChannel, times(1))
                .sendBatchNotification(any());
    }

    @Test
    @DisplayName("이메일 수신 거부 사용자는 알림에서 제외된다")
    void 이메일_수신_거부_사용자는_알림에서_제외된다() {
        // given
        final User emailOffUser = User.builder()
                .name("수신거부사용자")
                .email("off@test.com")
                .isEmailOn(false)
                .build();

        final Club club = Club.builder()
                .name("동아리 이름")
                .clubAffiliation(ClubAffiliation.CENTRAL_CLUB)
                .clubCategory(ClubCategory.CULTURAL_ART)
                .logo("로고")
                .description("설명")
                .instagram("인스타")
                .build();

        final Recruitment recruitment = Recruitment.builder()
                .club(club)
                .content("소개글")
                .recruitStart(LocalDateTime.now())
                .recruitEnd(LocalDateTime.now().plusDays(10))
                .build();

        final Favorite favorite = Favorite.builder()
                .user(emailOffUser)
                .club(club)
                .build();

        BDDMockito.given(favoriteRepository.findByClubIdWithFetchJoin(any()))
                .willReturn(List.of(favorite));

        // when
        emailService.sendBatchRecruitmentNotifications(List.of(recruitment));

        // then — 수신 거부 사용자만 있으므로 채널 호출 없음
        BDDMockito.verify(recruitmentNotificationChannel, times(0))
                .sendBatchNotification(any());
    }

    @Test
    @DisplayName("즐겨찾기 사용자가 없으면 채널을 호출하지 않는다")
    void 즐겨찾기_사용자가_없으면_채널을_호출하지_않는다() {
        // given
        final Club club = Club.builder()
                .name("동아리 이름")
                .clubAffiliation(ClubAffiliation.CENTRAL_CLUB)
                .clubCategory(ClubCategory.CULTURAL_ART)
                .logo("로고")
                .description("설명")
                .instagram("인스타")
                .build();

        final Recruitment recruitment = Recruitment.builder()
                .club(club)
                .content("소개글")
                .recruitStart(LocalDateTime.now())
                .recruitEnd(LocalDateTime.now().plusDays(10))
                .build();

        BDDMockito.given(favoriteRepository.findByClubIdWithFetchJoin(any()))
                .willReturn(List.of());

        // when
        emailService.sendBatchRecruitmentNotifications(List.of(recruitment));

        // then
        BDDMockito.verify(recruitmentNotificationChannel, times(0))
                .sendBatchNotification(any());
    }
}
