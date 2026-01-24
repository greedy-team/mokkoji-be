package com.greedy.mokkoji.comment.service;

import com.greedy.mokkoji.api.comment.dto.response.CommentListResponse;
import com.greedy.mokkoji.api.comment.service.CommentService;
import com.greedy.mokkoji.common.exception.MokkojiException;
import com.greedy.mokkoji.db.club.entity.Club;
import com.greedy.mokkoji.db.club.repository.ClubRepository;
import com.greedy.mokkoji.db.comment.entity.Comment;
import com.greedy.mokkoji.db.comment.repository.CommentRepository;
import com.greedy.mokkoji.db.user.entity.User;
import com.greedy.mokkoji.db.user.repository.UserRepository;
import com.greedy.mokkoji.enums.message.FailMessage;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

@DisplayName("댓글 서비스 테스트")
@ExtendWith(MockitoExtension.class)
public class CommentServiceTest {

    @InjectMocks
    private CommentService commentService;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private ClubRepository clubRepository;

    @Mock
    private UserRepository userRepository;

    @Test
    @DisplayName("댓글을 생성한다")
    void createComment() {
        //given
        final Long userId = 1L;
        final Long clubId = 1L;
        final Double rate = 5.0;
        final String content = "최고의 동아리입니다!";

        final User user = User.builder().build();
        ReflectionTestUtils.setField(user, "id", userId);

        final Club club = Club.builder().build();
        ReflectionTestUtils.setField(club, "id", clubId);

        BDDMockito.given(userRepository.findById(userId)).willReturn(Optional.of(user));
        BDDMockito.given(clubRepository.findById(clubId)).willReturn(Optional.of(club));
        BDDMockito.given(commentRepository.existsByClubAndUser(club, user)).willReturn(false);

        //when
        commentService.createComment(userId, clubId, rate, content);

        //then
        ArgumentCaptor<Comment> commentCaptor = ArgumentCaptor.forClass(Comment.class);
        BDDMockito.verify(commentRepository, times(1)).save(commentCaptor.capture());

        Comment savedComment = commentCaptor.getValue();
        assertThat(savedComment.getUser()).isEqualTo(user);
        assertThat(savedComment.getClub()).isEqualTo(club);
        assertThat(savedComment.getRate()).isEqualTo(rate);
        assertThat(savedComment.getContent()).isEqualTo(content);
    }

    @Test
    @DisplayName("이미 댓글이 존재하면 FORBIDDEN_ALREADY_EXIST_COMMENT 예외가 발생한다")
    void createComment_WhenCommentAlreadyExists_ThrowsException() {
        //given
        final Long userId = 1L;
        final Long clubId = 1L;
        final Double rate = 5.0;
        final String content = "댓글 내용";

        final User user = User.builder().build();
        ReflectionTestUtils.setField(user, "id", userId);

        final Club club = Club.builder().build();
        ReflectionTestUtils.setField(club, "id", clubId);

        BDDMockito.given(userRepository.findById(userId)).willReturn(Optional.of(user));
        BDDMockito.given(clubRepository.findById(clubId)).willReturn(Optional.of(club));
        BDDMockito.given(commentRepository.existsByClubAndUser(club, user)).willReturn(true);

        //when & then
        assertThatThrownBy(() -> commentService.createComment(userId, clubId, rate, content))
                .isInstanceOf(MokkojiException.class)
                .hasMessage(FailMessage.FORBIDDEN_ALREADY_EXIST_COMMENT.getMessage());

        BDDMockito.verify(commentRepository, never()).save(any());
    }

    @Test
    @DisplayName("동아리의 댓글 목록을 조회한다")
    void getComments() {
        //given
        final Long userId1 = 1L;
        final Long userId2 = 2L;
        final Long clubId = 1L;

        final User user1 = User.builder().build();
        ReflectionTestUtils.setField(user1, "id", userId1);

        final User user2 = User.builder().build();
        ReflectionTestUtils.setField(user2, "id", userId2);

        final Club club = Club.builder().build();
        ReflectionTestUtils.setField(club, "id", clubId);

        final Comment comment1 = Comment.builder()
                .user(user1)
                .club(club)
                .rate(5.0)
                .content("좋은 동아리입니다")
                .build();
        ReflectionTestUtils.setField(comment1, "id", 1L);
        ReflectionTestUtils.setField(comment1, "createdAt", LocalDateTime.of(2025, 11, 1, 10, 0));
        ReflectionTestUtils.setField(comment1, "updatedAt", LocalDateTime.of(2025, 11, 1, 10, 0));

        final Comment comment2 = Comment.builder()
                .user(user2)
                .club(club)
                .rate(4.0)
                .content("추천합니다")
                .build();
        ReflectionTestUtils.setField(comment2, "id", 2L);
        ReflectionTestUtils.setField(comment2, "createdAt", LocalDateTime.of(2025, 11, 2, 10, 0));
        ReflectionTestUtils.setField(comment2, "updatedAt", LocalDateTime.of(2025, 11, 2, 10, 0));

        BDDMockito.given(clubRepository.findById(clubId)).willReturn(Optional.of(club));
        BDDMockito.given(commentRepository.findAllByClub(club)).willReturn(List.of(comment1, comment2));

        //when
        CommentListResponse response = commentService.getComments(userId1, clubId);

        //then
        assertThat(response.comments()).hasSize(2);
        assertThat(response.comments())
                .extracting("id", "content", "rate", "isWriter")
                .containsExactlyInAnyOrder(
                        Tuple.tuple(1L, "좋은 동아리입니다", 5.0, true),
                        Tuple.tuple(2L, "추천합니다", 4.0, false)
                );

        BDDMockito.verify(clubRepository, times(1)).findById(clubId);
        BDDMockito.verify(commentRepository, times(1)).findAllByClub(club);
    }

    @Test
    @DisplayName("댓글을 수정한다")
    void updateComment() {
        //given
        final Long userId = 1L;
        final Long commentId = 1L;
        final Double newRate = 4.0;
        final String newContent = "수정된 댓글";

        final User user = User.builder().build();
        ReflectionTestUtils.setField(user, "id", userId);

        final Club club = Club.builder().build();
        ReflectionTestUtils.setField(club, "id", 1L);

        final Comment comment = Comment.builder()
                .user(user)
                .club(club)
                .rate(5.0)
                .content("원래 댓글")
                .build();
        ReflectionTestUtils.setField(comment, "id", commentId);

        BDDMockito.given(commentRepository.findById(commentId)).willReturn(Optional.of(comment));
        BDDMockito.given(userRepository.findById(userId)).willReturn(Optional.of(user));

        //when
        commentService.updateComment(userId, commentId, newRate, newContent);

        //then
        assertThat(comment.getRate()).isEqualTo(newRate);
        assertThat(comment.getContent()).isEqualTo(newContent);
    }

    @Test
    @DisplayName("댓글 수정 시 작성자가 아니면 FORBIDDEN_NOT_COMMENT_WRITER 예외가 발생한다")
    void updateComment_WithNotWriter_ThrowsException() {
        //given
        final Long userId = 1L;
        final Long commentId = 1L;
        final Double newRate = 4.0;
        final String newContent = "수정된 댓글";

        final User user = User.builder().build();
        ReflectionTestUtils.setField(user, "id", userId);

        final User anotherUser = User.builder().build();
        ReflectionTestUtils.setField(anotherUser, "id", 2L);

        final Club club = Club.builder().build();
        ReflectionTestUtils.setField(club, "id", 1L);

        final Comment comment = Comment.builder()
                .user(anotherUser)
                .club(club)
                .rate(5.0)
                .content("원래 댓글")
                .build();

        BDDMockito.given(commentRepository.findById(commentId)).willReturn(Optional.of(comment));
        BDDMockito.given(userRepository.findById(userId)).willReturn(Optional.of(user));

        //when & then
        assertThatThrownBy(() -> commentService.updateComment(userId, commentId, newRate, newContent))
                .isInstanceOf(MokkojiException.class)
                .hasMessage(FailMessage.FORBIDDEN_NOT_COMMENT_WRITER.getMessage());
    }

    @Test
    @DisplayName("댓글을 삭제한다")
    void deleteComment() {
        //given
        final Long userId = 1L;
        final Long commentId = 1L;

        final User user = User.builder().build();
        ReflectionTestUtils.setField(user, "id", userId);

        final Club club = Club.builder().build();
        ReflectionTestUtils.setField(club, "id", 1L);

        final Comment comment = Comment.builder()
                .user(user)
                .club(club)
                .rate(5.0)
                .content("댓글 내용")
                .build();
        ReflectionTestUtils.setField(comment, "id", commentId);

        BDDMockito.given(commentRepository.findById(commentId)).willReturn(Optional.of(comment));
        BDDMockito.given(userRepository.findById(userId)).willReturn(Optional.of(user));

        //when
        commentService.deleteComment(userId, commentId);

        //then
        BDDMockito.verify(commentRepository, times(1)).deleteById(commentId);
    }

    @Test
    @DisplayName("댓글 삭제 시 작성자가 아니면 FORBIDDEN_NOT_COMMENT_WRITER 예외가 발생한다")
    void deleteComment_WithNotWriter_ThrowsException() {
        //given
        final Long userId = 1L;
        final Long commentId = 1L;

        final User user = User.builder().build();
        ReflectionTestUtils.setField(user, "id", userId);

        final User anotherUser = User.builder().build();
        ReflectionTestUtils.setField(anotherUser, "id", 2L);

        final Club club = Club.builder().build();
        ReflectionTestUtils.setField(club, "id", 1L);

        final Comment comment = Comment.builder()
                .user(anotherUser)
                .club(club)
                .rate(5.0)
                .content("댓글 내용")
                .build();

        BDDMockito.given(commentRepository.findById(commentId)).willReturn(Optional.of(comment));
        BDDMockito.given(userRepository.findById(userId)).willReturn(Optional.of(user));

        //when & then
        assertThatThrownBy(() -> commentService.deleteComment(userId, commentId))
                .isInstanceOf(MokkojiException.class)
                .hasMessage(FailMessage.FORBIDDEN_NOT_COMMENT_WRITER.getMessage());

        BDDMockito.verify(commentRepository, never()).deleteById(any());
    }
}
