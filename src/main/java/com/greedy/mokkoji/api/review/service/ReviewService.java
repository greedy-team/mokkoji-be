package com.greedy.mokkoji.api.review.service;

import com.greedy.mokkoji.common.exception.MokkojiException;
import com.greedy.mokkoji.db.review.entity.Review;
import com.greedy.mokkoji.db.review.repository.ReviewRepository;
import com.greedy.mokkoji.db.user.entity.User;
import com.greedy.mokkoji.db.user.repository.UserRepository;
import com.greedy.mokkoji.enums.message.FailMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;

    @Transactional
    public Void createReport(Long userId, int rating, String content) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new MokkojiException(FailMessage.NOT_FOUND_USER));

        Review review = Review.builder()
                .userId(userId)
                .rating(rating)
                .content(content)
                .build();

        reviewRepository.save(review);
        return null;
    }
}
