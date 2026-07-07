package com.greedy.mokkoji.db.comment.repository;

import com.greedy.mokkoji.db.club.entity.Club;
import com.greedy.mokkoji.db.comment.entity.Comment;
import com.greedy.mokkoji.db.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findAllByClub(final Club club);

    @Query("SELECT c FROM Comment c JOIN FETCH c.club WHERE c.user = :user")
    List<Comment> findAllByUser(final User user);

    boolean existsByClubAndUser(final Club club, final User user);

    @Modifying
    @Query("UPDATE Comment c SET c.user = null WHERE c.user.id = :userId")
    void detachUserByUserId(final Long userId);

    void deleteByClubId(final Long clubId);
}
