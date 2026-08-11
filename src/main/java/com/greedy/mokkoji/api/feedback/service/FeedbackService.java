package com.greedy.mokkoji.api.feedback.service;

import com.greedy.mokkoji.db.feedback.entity.Feedback;
import com.greedy.mokkoji.db.feedback.repository.FeedbackRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
public class FeedbackService {

    private final FeedbackRepository feedbackRepository;

    @Transactional
    public Void createFeedback(int rating, String content) {

        Feedback feedback = Feedback.builder()
                .rating(rating)
                .content(content)
                .build();

        feedbackRepository.save(feedback);
        return null;
    }
}
