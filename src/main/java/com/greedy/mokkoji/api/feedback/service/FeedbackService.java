package com.greedy.mokkoji.api.feedback.service;

import com.greedy.mokkoji.common.exception.MokkojiException;
import com.greedy.mokkoji.db.feedback.entity.Feedback;
import com.greedy.mokkoji.db.feedback.repository.FeedbackRepository;
import com.greedy.mokkoji.db.user.entity.User;
import com.greedy.mokkoji.db.user.repository.UserRepository;
import com.greedy.mokkoji.enums.message.FailMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
public class FeedbackService {

    private final FeedbackRepository feedbackRepository;
    private final UserRepository userRepository;

    @Transactional
    public Void createFeedback(Long userId, int rating, String content) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new MokkojiException(FailMessage.NOT_FOUND_USER));

        Feedback feedback = Feedback.builder()
                .userId(userId)
                .rating(rating)
                .content(content)
                .build();

        feedbackRepository.save(feedback);
        return null;
    }
}
