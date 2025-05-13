package com.greedy.mokkoji.api.comment.service;

import com.greedy.mokkoji.api.comment.dto.response.CommentListResponse;
import com.greedy.mokkoji.api.comment.dto.response.CommentResponse;
import com.greedy.mokkoji.common.exception.MokkojiException;
import com.greedy.mokkoji.db.club.entity.Club;
import com.greedy.mokkoji.db.club.repository.ClubRepository;
import com.greedy.mokkoji.db.commenmt.entity.Comment;
import com.greedy.mokkoji.db.commenmt.repository.CommentRepository;
import com.greedy.mokkoji.db.user.entity.User;
import com.greedy.mokkoji.db.user.repository.UserRepository;
import com.greedy.mokkoji.enums.message.FailMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final ClubRepository clubRepository;
    private final UserRepository userRepository;

    @Transactional
    public void createComment(final Long userId, final Long clubId, final Double rate, final String content) {
        final User user = userRepository.findById(userId).orElseThrow(
                () -> new MokkojiException(FailMessage.NOT_FOUND_USER)
        );

        final Club club = clubRepository.findById(clubId).orElseThrow(
                () -> new MokkojiException(FailMessage.NOT_FOUND_CLUB)
        );

        commentRepository.save(
                Comment.builder()
                        .user(user)
                        .club(club)
                        .rate(rate)
                        .content(content)
                        .build()
        );
    }

    @Transactional(readOnly = true)
    public CommentListResponse getComments(final Long userId, final Long clubId) {
        final Club club = clubRepository.findById(clubId).orElseThrow(
                () -> new MokkojiException(FailMessage.NOT_FOUND_CLUB)
        );

        final List<Comment> comments = commentRepository.findAllByClub(club);

        return CommentListResponse.of(
                comments.stream()
                        .map(comment -> CommentResponse.of(
                                comment.getId(),
                                comment.getContent(),
                                comment.getRate(),
                                comment.getCreatedAt() == comment.getUpdatedAt(),
                                comment.getCreatedAt() != comment.getUpdatedAt() ? comment.getUpdatedAt() : comment.getCreatedAt(),
                                isCommentWriter(userId, comment)
                        )).toList()
        );

    }

    private boolean isCommentWriter(final Long userId, final Comment comment) {
        if (userId == null) {
            return false;
        }
        return comment.getUser().getId().equals(userId);
    }

}
