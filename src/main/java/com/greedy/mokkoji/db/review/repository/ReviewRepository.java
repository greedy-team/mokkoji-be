package com.greedy.mokkoji.db.review.repository;

import com.greedy.mokkoji.db.review.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRepository extends JpaRepository<Review, Long> {
}
