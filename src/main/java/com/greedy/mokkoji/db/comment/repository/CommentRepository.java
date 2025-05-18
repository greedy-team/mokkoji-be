package com.greedy.mokkoji.db.comment.repository;

import com.greedy.mokkoji.db.club.entity.Club;
import com.greedy.mokkoji.db.comment.entity.Comment;
import com.greedy.mokkoji.db.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findAllByClub(final Club club);

    boolean existsByClubAndUser(final Club club, final User user);
}
