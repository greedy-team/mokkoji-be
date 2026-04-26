package com.greedy.mokkoji.db.report.repository;

import com.greedy.mokkoji.db.report.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRepository extends JpaRepository<Review, Long> {
}
