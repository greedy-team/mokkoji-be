package com.greedy.mokkoji.feedback.service;

import com.greedy.mokkoji.api.email.dto.FeedbackNotification;
import com.greedy.mokkoji.api.email.service.DiscordNotifier;
import com.greedy.mokkoji.api.external.AfterCommitExecutor;
import com.greedy.mokkoji.api.feedback.service.FeedbackService;
import com.greedy.mokkoji.common.exception.MokkojiException;
import com.greedy.mokkoji.db.feedback.entity.Feedback;
import com.greedy.mokkoji.db.feedback.repository.FeedbackRepository;
import com.greedy.mokkoji.db.user.repository.RedisRepository;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@DisplayName("피드백 서비스 테스트")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class FeedbackServiceTest {

    @InjectMocks
    FeedbackService feedbackService;

    @Mock
    FeedbackRepository feedbackRepository;

    @Mock
    DiscordNotifier discordNotifier;

    @Mock
    AfterCommitExecutor afterCommitExecutor;

    @Mock
    RedisRepository redisRepository;

    @Test
    @DisplayName("피드백을 저장하고 커밋 후 디스코드 알림을 발송한다.")
    void createFeedback() {
        // given
        final Feedback saved = Feedback.builder()
                .rating(4)
                .content("목오지 쵝오")
                .school("세종대학교")
                .build();
        given(redisRepository.setIfAbsent(anyString(), anyString(), anyLong())).willReturn(true);
        given(feedbackRepository.save(any(Feedback.class))).willReturn(saved);
        BDDMockito.willAnswer(invocation -> {
            invocation.getArgument(0, Runnable.class).run();
            return null;
        }).given(afterCommitExecutor).run(any(Runnable.class));

        // when
        feedbackService.createFeedback(4, "목오지 쵝오", "세종대학교", "127.0.0.1");

        // then
        final ArgumentCaptor<Feedback> savedCaptor = ArgumentCaptor.forClass(Feedback.class);
        BDDMockito.then(feedbackRepository).should().save(savedCaptor.capture());
        assertThat(savedCaptor.getValue().getRating()).isEqualTo(4);
        assertThat(savedCaptor.getValue().getContent()).isEqualTo("목오지 쵝오");
        assertThat(savedCaptor.getValue().getSchool()).isEqualTo("세종대학교");

        final ArgumentCaptor<FeedbackNotification> notificationCaptor =
                ArgumentCaptor.forClass(FeedbackNotification.class);
        BDDMockito.then(discordNotifier).should().notifyFeedbackCreated(notificationCaptor.capture());
        assertThat(notificationCaptor.getValue().rating()).isEqualTo(4);
        assertThat(notificationCaptor.getValue().content()).isEqualTo("목오지 쵝오");
        assertThat(notificationCaptor.getValue().school()).isEqualTo("세종대학교");
    }

    @Test
    @DisplayName("커밋 전에는 디스코드 알림을 발송하지 않는다.")
    void doNotNotifyBeforeCommit() {
        // given
        given(redisRepository.setIfAbsent(anyString(), anyString(), anyLong())).willReturn(true);
        given(feedbackRepository.save(any(Feedback.class)))
                .willReturn(Feedback.builder().rating(5).content("좋아요").school("세종대학교").build());

        // when
        feedbackService.createFeedback(5, "좋아요", "세종대학교", "127.0.0.1");

        // then
        BDDMockito.then(discordNotifier).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("3초 이내 같은 IP의 중복 요청은 예외를 던진다.")
    void throwExceptionOnCooldown() {
        // given
        given(redisRepository.setIfAbsent(anyString(), anyString(), anyLong())).willReturn(false);

        // when & then
        assertThatThrownBy(() -> feedbackService.createFeedback(4, "목오지 쵝오", "세종대학교", "127.0.0.1"))
                .isInstanceOf(MokkojiException.class);
        BDDMockito.then(feedbackRepository).shouldHaveNoInteractions();
        BDDMockito.then(discordNotifier).shouldHaveNoInteractions();
    }
}
