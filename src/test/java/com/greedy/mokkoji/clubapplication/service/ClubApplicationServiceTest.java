package com.greedy.mokkoji.clubapplication.service;

import com.greedy.mokkoji.api.clubapplication.dto.request.ClubApplicationCreateRequest;
import com.greedy.mokkoji.api.clubapplication.service.ClubApplicationService;
import com.greedy.mokkoji.api.email.dto.ClubApplicationNotification;
import com.greedy.mokkoji.api.email.service.DiscordNotifier;
import com.greedy.mokkoji.api.external.AfterCommitExecutor;
import com.greedy.mokkoji.api.external.AppDataS3Client;
import com.greedy.mokkoji.common.exception.MokkojiException;
import com.greedy.mokkoji.common.fixture.Fixture;
import com.greedy.mokkoji.db.clubapplication.entity.ClubApplication;
import com.greedy.mokkoji.db.clubapplication.repository.ClubApplicationRepository;
import com.greedy.mokkoji.db.university.entity.University;
import com.greedy.mokkoji.db.university.repository.UniversityRepository;
import com.greedy.mokkoji.db.user.entity.User;
import com.greedy.mokkoji.db.user.repository.UserRepository;
import com.greedy.mokkoji.enums.club.ClubAffiliation;
import com.greedy.mokkoji.enums.club.ClubCategory;
import com.greedy.mokkoji.enums.message.FailMessage;
import com.greedy.mokkoji.enums.university.UniversityCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
@DisplayName("동아리 생성 신청 서비스 테스트")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
public class ClubApplicationServiceTest {

    private static final Long USER_ID = 1L;
    private static final Long APPLICATION_ID = 100L;

    @InjectMocks
    ClubApplicationService clubApplicationService;

    @Mock
    ClubApplicationRepository clubApplicationRepository;

    @Mock
    UserRepository userRepository;

    @Mock
    UniversityRepository universityRepository;

    @Mock
    AppDataS3Client appDataS3Client;

    @Mock
    DiscordNotifier discordNotifier;

    @Mock
    AfterCommitExecutor afterCommitExecutor;

    private final University university = Fixture.createUniversity();
    private final User user = Fixture.createUser(university);

    private ClubApplicationCreateRequest createRequest() {
        return new ClubApplicationCreateRequest(
                UniversityCode.SEJONG,
                "그리디",
                "홍길동",
                ClubCategory.ACADEMIC_CULTURAL,
                ClubAffiliation.CENTRAL_CLUB,
                "logo.png",
                "https://instagram.com/greedy",
                "알고리즘을 공부하는 동아리입니다."
        );
    }

    @Test
    @DisplayName("동아리 생성 신청이 완료되면 커밋 이후 디스코드 알림이 발송된다.")
    void createClubApplicationSendsDiscordNotification() {
        // given
        BDDMockito.given(userRepository.findById(USER_ID)).willReturn(Optional.of(user));
        BDDMockito.given(universityRepository.findByCode(UniversityCode.SEJONG)).willReturn(Optional.of(university));
        BDDMockito.given(clubApplicationRepository.existsByApplicantAndUniversityAndClubNameAndStatusNot(any(), any(), any(), any()))
                .willReturn(false);
        BDDMockito.willAnswer(invocation -> {
            final ClubApplication saved = invocation.getArgument(0);
            ReflectionTestUtils.setField(saved, "id", APPLICATION_ID);
            return saved;
        }).given(clubApplicationRepository).save(any(ClubApplication.class));
        BDDMockito.willAnswer(invocation -> {
            invocation.getArgument(0, Runnable.class).run();
            return null;
        }).given(afterCommitExecutor).run(any(Runnable.class));

        // when
        clubApplicationService.createClubApplication(USER_ID, createRequest());

        // then
        final ArgumentCaptor<ClubApplicationNotification> captor =
                ArgumentCaptor.forClass(ClubApplicationNotification.class);
        BDDMockito.verify(discordNotifier).notifyClubApplicationCreated(captor.capture());

        final ClubApplicationNotification notification = captor.getValue();
        assertThat(notification.applicationId()).isEqualTo(APPLICATION_ID);
        assertThat(notification.universityName()).isEqualTo("세종대학교");
        assertThat(notification.clubName()).isEqualTo("그리디");
        assertThat(notification.clubCategory()).isEqualTo(ClubCategory.ACADEMIC_CULTURAL.getDescription());
        assertThat(notification.clubAffiliation()).isEqualTo(ClubAffiliation.CENTRAL_CLUB.getDescription());
        assertThat(notification.applicantName()).isEqualTo("홍길동");
        assertThat(notification.applicantEmail()).isEqualTo(user.getEmail());
    }

    @Test
    @DisplayName("중복 신청으로 실패하면 디스코드 알림이 발송되지 않는다.")
    void doNotNotifyWhenDuplicateApplication() {
        // given
        BDDMockito.given(userRepository.findById(USER_ID)).willReturn(Optional.of(user));
        BDDMockito.given(universityRepository.findByCode(UniversityCode.SEJONG)).willReturn(Optional.of(university));
        BDDMockito.given(clubApplicationRepository.existsByApplicantAndUniversityAndClubNameAndStatusNot(any(), any(), any(), any()))
                .willReturn(true);

        // when & then
        assertThatThrownBy(() -> clubApplicationService.createClubApplication(USER_ID, createRequest()))
                .isInstanceOf(MokkojiException.class)
                .hasMessage(FailMessage.CONFLICT_CLUB_APPLICATION.getMessage());

        BDDMockito.verifyNoInteractions(discordNotifier);
    }
}
