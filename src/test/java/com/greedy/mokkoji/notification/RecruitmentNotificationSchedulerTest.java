package com.greedy.mokkoji.notification;

import com.greedy.mokkoji.api.notification.service.NotificationService;
import com.greedy.mokkoji.api.scheduler.service.RecruitmentNotificationScheduler;
import com.greedy.mokkoji.db.club.entity.Club;
import com.greedy.mokkoji.db.recruitment.entity.Recruitment;
import com.greedy.mokkoji.db.recruitment.repository.RecruitmentRepository;
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
@DisplayName("스케줄러 테스트")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
public class RecruitmentNotificationSchedulerTest {
    @InjectMocks
    RecruitmentNotificationScheduler recruitmentNotificationScheduler;

    @Mock
    RecruitmentRepository recruitmentRepository;

    @Mock
    NotificationService notificationService;

    @Test
    @DisplayName("모집 공고가 오늘인 동아리를 찾아 알림을 보낸다")
    void findRecruitmentAndSendNotification() {
        // given
        final LocalDateTime currentDateTime = LocalDateTime.now();

        final Club club1 = Club.builder()
                .name("동아리 이름1")
                .clubAffiliation(ClubAffiliation.CENTRAL_CLUB)
                .clubCategory(ClubCategory.ETC)
                .logo("동아리 로고1")
                .description("동아리 설명1")
                .instagram("동아리 인스타 링크1")
                .build();

        final Club club2 = Club.builder()
                .name("동아리 이름2")
                .clubAffiliation(ClubAffiliation.CENTRAL_CLUB)
                .clubCategory(ClubCategory.ETC)
                .logo("동아리 로고2")
                .description("동아리 설명2")
                .instagram("동아리 인스타 링크2")
                .build();

        final Recruitment recruitment1 = Recruitment.builder()
                .club(club1)
                .recruitStart(currentDateTime)
                .recruitEnd(currentDateTime.plusDays(10))
                .content("모집글1")
                .build();

        final Recruitment recruitment2 = Recruitment.builder()
                .club(club2)
                .recruitStart(currentDateTime)
                .recruitEnd(currentDateTime.plusDays(10))
                .content("모집글2")
                .build();

        BDDMockito.given(recruitmentRepository.findTodayRecruitStartDate(currentDateTime.toLocalDate()))
                .willReturn(List.of(recruitment1, recruitment2));

        BDDMockito.doNothing().when(notificationService).sendNotification(any(Club.class), any(Recruitment.class));

        // when
        recruitmentNotificationScheduler.sendDailyRecruitmentNotifications();

        //then
        BDDMockito.verify(recruitmentRepository, times(1))
                .findTodayRecruitStartDate(currentDateTime.toLocalDate());

        BDDMockito.verify(notificationService, times(2)).sendNotification(any(Club.class), any(Recruitment.class));
    }
}
