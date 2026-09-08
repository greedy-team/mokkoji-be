package com.greedy.mokkoji.api.email.dto;

import com.greedy.mokkoji.db.feedback.entity.Feedback;

public record FeedbackNotification(
        Long feedbackId,
        int rating,
        String content
) {

    public static FeedbackNotification from(final Feedback feedback) {
        return new FeedbackNotification(
                feedback.getId(),
                feedback.getRating(),
                feedback.getContent()
        );
    }
}
