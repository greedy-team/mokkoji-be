package com.greedy.mokkoji.db.feedback.repository;

import com.greedy.mokkoji.db.feedback.entity.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FeedbackRepository extends JpaRepository<Feedback, Long> {
}
