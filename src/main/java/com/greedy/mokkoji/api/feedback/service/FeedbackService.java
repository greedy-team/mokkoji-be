package com.greedy.mokkoji.api.feedback.service;

import com.greedy.mokkoji.api.email.dto.FeedbackNotification;
import com.greedy.mokkoji.api.email.service.DiscordNotifier;
import com.greedy.mokkoji.api.external.AfterCommitExecutor;
import com.greedy.mokkoji.db.feedback.entity.Feedback;
import com.greedy.mokkoji.db.feedback.repository.FeedbackRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
public class FeedbackService {

    private final FeedbackRepository feedbackRepository;
    private final DiscordNotifier discordNotifier;
    private final AfterCommitExecutor afterCommitExecutor;

    @Transactional
    public Void createFeedback(int rating, String content) {

        Feedback feedback = Feedback.builder()
                .rating(rating)
                .content(content)
                .build();

        final Feedback savedFeedback = feedbackRepository.save(feedback);
        notifyFeedbackCreated(savedFeedback);
        return null;
    }

    private void notifyFeedbackCreated(final Feedback feedback) {
        final FeedbackNotification notification = FeedbackNotification.from(feedback);

        afterCommitExecutor.run(() -> discordNotifier.notifyFeedbackCreated(notification));
    }
}
