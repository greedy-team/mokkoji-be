package com.greedy.mokkoji.comment.controller;

import com.greedy.mokkoji.api.comment.dto.request.CommentCreateRequest;
import com.greedy.mokkoji.api.comment.dto.request.CommentUpdateRequest;
import com.greedy.mokkoji.api.comment.dto.response.CommentListResponse;
import com.greedy.mokkoji.common.ControllerTest;
import com.greedy.mokkoji.common.fixture.Fixture;
import com.greedy.mokkoji.common.response.APIErrorResponse;
import com.greedy.mokkoji.db.club.entity.Club;
import com.greedy.mokkoji.db.comment.entity.Comment;
import com.greedy.mokkoji.db.user.entity.User;
import com.greedy.mokkoji.enums.message.FailMessage;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

public class CommentControllerTest extends ControllerTest {

    private User user;
    private User anotherUser;
    private Club club;

    @BeforeEach
    void setUp() {
        commentRepository.deleteAll();
        favoriteRepository.deleteAll();
        recruitmentRepository.deleteAll();
        clubRepository.deleteAll();
        userRepository.deleteAll();
        prepareData();
    }

    private void prepareData() {
        user = userRepository.save(Fixture.createUser());
        anotherUser = userRepository.save(Fixture.createAnotherUser());
        club = clubRepository.save(Fixture.createClub());
    }

    @Test
    @DisplayName("댓글을 생성할 수 있다.")
    void createComment() {
        //given
        String authorizationForBearer = authorizationForBearerAccessToken(user);
        final CommentCreateRequest request = new CommentCreateRequest(5.0, "최고의 동아리입니다!");

        //when
        final ExtractableResponse<Response> response = given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header("Authorization", authorizationForBearer)
                .body(request)
                .when().post(prefixUrl + "/comments/{clubId}", club.getId())
                .then().log().all()
                .extract();

        //then
        final int statusCode = response.statusCode();
        assertThat(statusCode).isEqualTo(HttpStatus.CREATED.value());

        final Comment createdComment = commentRepository.findAll().get(0);
        assertThat(createdComment.getUser().getId()).isEqualTo(user.getId());
        assertThat(createdComment.getClub().getId()).isEqualTo(club.getId());
        assertThat(createdComment.getRate()).isEqualTo(5.0);
        assertThat(createdComment.getContent()).isEqualTo("최고의 동아리입니다!");
    }

    @Test
    @DisplayName("이미 댓글이 존재하면 403를 반환한다.")
    void createComment_WhenAlreadyExists() {
        //given
        final Comment existingComment = Comment.builder()
                .user(user)
                .club(club)
                .rate(4.0)
                .content("기존 댓글")
                .build();
        commentRepository.save(existingComment);

        String authorizationForBearer = authorizationForBearerAccessToken(user);
        final CommentCreateRequest request = new CommentCreateRequest(5.0, "새로운 댓글");

        //when
        final ExtractableResponse<Response> response = given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header("Authorization", authorizationForBearer)
                .body(request)
                .when().post(prefixUrl + "/comments/{clubId}", club.getId())
                .then().log().all()
                .extract();

        //then
        final int actualStatusCode = response.statusCode();
        final APIErrorResponse actualResponse = response.as(APIErrorResponse.class);
        final APIErrorResponse expectedResponse = new APIErrorResponse(
                FailMessage.FORBIDDEN_ALREADY_EXIST_COMMENT.getCode(),
                FailMessage.FORBIDDEN_ALREADY_EXIST_COMMENT.getMessage()
        );

        assertThat(actualStatusCode).isEqualTo(HttpStatus.FORBIDDEN.value());
        assertThat(actualResponse).usingRecursiveComparison().isEqualTo(expectedResponse);
    }

    @Test
    @DisplayName("동아리의 댓글 목록을 조회할 수 있다.")
    void getComments() {
        //given
        final Comment comment1 = Comment.builder()
                .user(user)
                .club(club)
                .rate(5.0)
                .content("좋은 동아리입니다")
                .build();
        final Comment comment2 = Comment.builder()
                .user(anotherUser)
                .club(club)
                .rate(4.0)
                .content("추천합니다")
                .build();
        commentRepository.save(comment1);
        commentRepository.save(comment2);

        String authorizationForBearer = authorizationForBearerAccessToken(user);

        //when
        final ExtractableResponse<Response> response = given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header("Authorization", authorizationForBearer)
                .when().get(prefixUrl + "/comments/{clubId}", club.getId())
                .then().log().all()
                .extract();

        //then
        final int statusCode = response.statusCode();
        final CommentListResponse actual = getDataFromResponse(response, CommentListResponse.class);

        assertThat(statusCode).isEqualTo(HttpStatus.OK.value());
        assertThat(actual.comments()).hasSize(2);
        assertThat(actual.comments().get(0).content()).isEqualTo("좋은 동아리입니다");
        assertThat(actual.comments().get(0).rate()).isEqualTo(5.0);
        assertThat(actual.comments().get(0).isWriter()).isTrue();
        assertThat(actual.comments().get(1).content()).isEqualTo("추천합니다");
        assertThat(actual.comments().get(1).isWriter()).isFalse();
    }

    @Test
    @DisplayName("댓글을 수정할 수 있다.")
    void updateComment() {
        //given
        final Comment comment = Comment.builder()
                .user(user)
                .club(club)
                .rate(5.0)
                .content("원래 댓글")
                .build();
        commentRepository.save(comment);

        String authorizationForBearer = authorizationForBearerAccessToken(user);
        final CommentUpdateRequest request = new CommentUpdateRequest(4.0, "수정된 댓글");

        //when
        final ExtractableResponse<Response> response = given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header("Authorization", authorizationForBearer)
                .body(request)
                .when().patch(prefixUrl + "/comments/{commentId}", comment.getId())
                .then().log().all()
                .extract();

        //then
        final int statusCode = response.statusCode();
        assertThat(statusCode).isEqualTo(HttpStatus.OK.value());

        final Comment updatedComment = commentRepository.findById(comment.getId()).orElseThrow();
        assertThat(updatedComment.getRate()).isEqualTo(4.0);
        assertThat(updatedComment.getContent()).isEqualTo("수정된 댓글");
    }

    @Test
    @DisplayName("작성자가 아니면 댓글 수정 시 403을 반환한다.")
    void updateComment_WhenNotWriter() {
        //given
        final Comment comment = Comment.builder()
                .user(anotherUser)
                .club(club)
                .rate(5.0)
                .content("다른 사람의 댓글")
                .build();
        commentRepository.save(comment);

        String authorizationForBearer = authorizationForBearerAccessToken(user);
        final CommentUpdateRequest request = new CommentUpdateRequest(4.0, "수정 시도");

        //when
        final ExtractableResponse<Response> response = given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header("Authorization", authorizationForBearer)
                .body(request)
                .when().patch(prefixUrl + "/comments/{commentId}", comment.getId())
                .then().log().all()
                .extract();

        //then
        final int actualStatusCode = response.statusCode();
        final APIErrorResponse actualResponse = response.as(APIErrorResponse.class);
        final APIErrorResponse expectedResponse = new APIErrorResponse(
                FailMessage.FORBIDDEN_NOT_COMMENT_WRITER.getCode(),
                FailMessage.FORBIDDEN_NOT_COMMENT_WRITER.getMessage()
        );

        assertThat(actualStatusCode).isEqualTo(HttpStatus.FORBIDDEN.value());
        assertThat(actualResponse).usingRecursiveComparison().isEqualTo(expectedResponse);
    }

    @Test
    @DisplayName("댓글을 삭제할 수 있다.")
    void deleteComment() {
        //given
        final Comment comment = Comment.builder()
                .user(user)
                .club(club)
                .rate(5.0)
                .content("삭제할 댓글")
                .build();
        commentRepository.save(comment);

        String authorizationForBearer = authorizationForBearerAccessToken(user);

        //when
        final ExtractableResponse<Response> response = given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header("Authorization", authorizationForBearer)
                .when().delete(prefixUrl + "/comments/{commentId}", comment.getId())
                .then().log().all()
                .extract();

        //then
        final int statusCode = response.statusCode();
        assertThat(statusCode).isEqualTo(HttpStatus.OK.value());
        assertThat(commentRepository.findById(comment.getId())).isEmpty();
    }

    @Test
    @DisplayName("작성자가 아니면 댓글 삭제 시 403을 반환한다.")
    void deleteComment_WhenNotWriter() {
        //given
        final Comment comment = Comment.builder()
                .user(anotherUser)
                .club(club)
                .rate(5.0)
                .content("다른 사람의 댓글")
                .build();
        commentRepository.save(comment);

        String authorizationForBearer = authorizationForBearerAccessToken(user);

        //when
        final ExtractableResponse<Response> response = given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header("Authorization", authorizationForBearer)
                .when().delete(prefixUrl + "/comments/{commentId}", comment.getId())
                .then().log().all()
                .extract();

        //then
        final int actualStatusCode = response.statusCode();
        final APIErrorResponse actualResponse = response.as(APIErrorResponse.class);
        final APIErrorResponse expectedResponse = new APIErrorResponse(
                FailMessage.FORBIDDEN_NOT_COMMENT_WRITER.getCode(),
                FailMessage.FORBIDDEN_NOT_COMMENT_WRITER.getMessage()
        );

        assertThat(actualStatusCode).isEqualTo(HttpStatus.FORBIDDEN.value());
        assertThat(actualResponse).usingRecursiveComparison().isEqualTo(expectedResponse);
    }
}
