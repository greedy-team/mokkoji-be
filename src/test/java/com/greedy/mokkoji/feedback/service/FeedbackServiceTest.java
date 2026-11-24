package com.greedy.mokkoji.feedback.service;

import com.greedy.mokkoji.api.email.dto.FeedbackNotification;
import com.greedy.mokkoji.api.email.service.DiscordNotifier;
import com.greedy.mokkoji.api.external.AfterCommitExecutor;
import com.greedy.mokkoji.api.feedback.service.FeedbackService;
import com.greedy.mokkoji.db.feedback.entity.Feedback;
import com.greedy.mokkoji.db.feedback.repository.FeedbackRepository;
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
import static org.mockito.ArgumentMatchers.any;
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

    @Test
    @DisplayName("피드백을 저장하고 커밋 후 디스코드 알림을 발송한다.")
    void createFeedback() {
        // given
        final Feedback saved = Feedback.builder()
                .rating(4)
                .content("목오지 쵝오")
                .build();
        given(feedbackRepository.save(any(Feedback.class))).willReturn(saved);
        // afterCommit 콜백을 즉시 실행하도록 스텁
        BDDMockito.willAnswer(invocation -> {
            invocation.getArgument(0, Runnable.class).run();
            return null;
        }).given(afterCommitExecutor).run(any(Runnable.class));

        // when
        feedbackService.createFeedback(4, "목오지 쵝오");

        // then
        final ArgumentCaptor<Feedback> savedCaptor = ArgumentCaptor.forClass(Feedback.class);
        BDDMockito.then(feedbackRepository).should().save(savedCaptor.capture());
        assertThat(savedCaptor.getValue().getRating()).isEqualTo(4);
        assertThat(savedCaptor.getValue().getContent()).isEqualTo("목오지 쵝오");

        final ArgumentCaptor<FeedbackNotification> notificationCaptor =
                ArgumentCaptor.forClass(FeedbackNotification.class);
        BDDMockito.then(discordNotifier).should().notifyFeedbackCreated(notificationCaptor.capture());
        assertThat(notificationCaptor.getValue().rating()).isEqualTo(4);
        assertThat(notificationCaptor.getValue().content()).isEqualTo("목오지 쵝오");
    }

    @Test
    @DisplayName("커밋 전에는 디스코드 알림을 발송하지 않는다.")
    void doNotNotifyBeforeCommit() {
        // given
        given(feedbackRepository.save(any(Feedback.class)))
                .willReturn(Feedback.builder().rating(5).content("좋아요").build());
        // afterCommitExecutor가 콜백을 실행하지 않는 상황(커밋 전)

        // when
        feedbackService.createFeedback(5, "좋아요");

        // then
        BDDMockito.then(discordNotifier).shouldHaveNoInteractions();
    }
}
