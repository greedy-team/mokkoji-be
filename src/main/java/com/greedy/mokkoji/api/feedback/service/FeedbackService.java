package com.greedy.mokkoji.api.feedback.service;

import com.greedy.mokkoji.api.email.dto.FeedbackNotification;
import com.greedy.mokkoji.api.email.service.DiscordNotifier;
import com.greedy.mokkoji.api.external.AfterCommitExecutor;
import com.greedy.mokkoji.common.exception.MokkojiException;
import com.greedy.mokkoji.db.feedback.entity.Feedback;
import com.greedy.mokkoji.db.feedback.repository.FeedbackRepository;
import com.greedy.mokkoji.db.user.repository.RedisRepository;
import com.greedy.mokkoji.enums.message.FailMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
public class FeedbackService {

    private static final long FEEDBACK_COOLDOWN_MILLIS = 3_000L;
    private static final String FEEDBACK_COOLDOWN_KEY_PREFIX = "feedback:cooldown:";

    private final FeedbackRepository feedbackRepository;
    private final DiscordNotifier discordNotifier;
    private final AfterCommitExecutor afterCommitExecutor;
    private final RedisRepository redisRepository;

    @Transactional
    public Void createFeedback(int rating, String content, String school, String clientIp) {
        checkCooldown(clientIp);

        Feedback feedback = Feedback.builder()
                .rating(rating)
                .content(content)
                .school(school)
                .build();

        final Feedback savedFeedback = feedbackRepository.save(feedback);
        notifyFeedbackCreated(savedFeedback);
        return null;
    }

    private void checkCooldown(final String clientIp) {
        final String key = FEEDBACK_COOLDOWN_KEY_PREFIX + clientIp;
        if (!redisRepository.setIfAbsent(key, "1", FEEDBACK_COOLDOWN_MILLIS)) {
            throw new MokkojiException(FailMessage.TOO_MANY_REQUESTS);
        }
    }

    private void notifyFeedbackCreated(final Feedback feedback) {
        final FeedbackNotification notification = FeedbackNotification.from(feedback);

        afterCommitExecutor.run(() -> discordNotifier.notifyFeedbackCreated(notification));
    }
}
