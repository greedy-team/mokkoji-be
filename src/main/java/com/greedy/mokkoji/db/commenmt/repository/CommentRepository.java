package com.greedy.mokkoji.db.commenmt.repository;

import com.greedy.mokkoji.db.commenmt.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Long> {
}
