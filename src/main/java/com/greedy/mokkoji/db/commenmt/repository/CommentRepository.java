package com.greedy.mokkoji.db.commenmt.repository;

import com.greedy.mokkoji.db.club.entity.Club;
import com.greedy.mokkoji.db.commenmt.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findAllByClub(final Club club);
}
