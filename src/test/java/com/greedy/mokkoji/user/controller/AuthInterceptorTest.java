package com.greedy.mokkoji.user.controller;

import com.greedy.mokkoji.common.ControllerTest;
import com.greedy.mokkoji.common.fixture.Fixture;
import com.greedy.mokkoji.common.response.APIErrorResponse;
import com.greedy.mokkoji.db.user.entity.User;
import com.greedy.mokkoji.enums.message.FailMessage;
import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("NonAsciiCharacters")
public class AuthInterceptorTest extends ControllerTest {

    User user;

    @BeforeEach
    void setUp() {
        prepareData();
    }

    private void prepareData() {
        user = userRepository.save(Fixture.createUser());
    }

    @Test
    void 인증_헤더_정보가_존재하지_않을때_401을_응답한다() {
        // given & when
        final ExtractableResponse<Response> response = RestAssured.given()
                .log().all()
                .when()
                .get("/users")
                .then()
                .log().all()
                .extract();

        // then
        final int actualStatusCode = response.statusCode();
        final int expectedStatusCode = HttpStatus.UNAUTHORIZED.value();
        final APIErrorResponse actualResponse = response.as(APIErrorResponse.class);
        final APIErrorResponse expectedResponse = new APIErrorResponse(FailMessage.UNAUTHORIZED_EMPTY_HEADER.getCode(), FailMessage.UNAUTHORIZED_EMPTY_HEADER.getMessage());

        assertThat(actualStatusCode).isEqualTo(expectedStatusCode);
        assertThat(actualResponse).usingRecursiveComparison().isEqualTo(expectedResponse);
    }

    @Test
    void 인증_헤더_정보가_Bearer가_아닐때_401을_응답한다() {
        String accessToken = jwtUtil.generateAccessToken(user.getId());
        // when
        final ExtractableResponse<Response> response = RestAssured.given().log().all()
                .when().header("Authorization", "Basic " + accessToken)
                .get("/users")
                .then()
                .log().all()
                .extract();

        // then
        final int actualStatusCode = response.statusCode();
        final int expectedStatusCode = HttpStatus.UNAUTHORIZED.value();
        final APIErrorResponse actualResponse = response.as(APIErrorResponse.class);
        final APIErrorResponse expectedResponse = new APIErrorResponse(FailMessage.UNAUTHORIZED_INVALID_TOKEN.getCode(), FailMessage.UNAUTHORIZED_INVALID_TOKEN.getMessage());

        assertThat(actualStatusCode).isEqualTo(expectedStatusCode);
        assertThat(actualResponse).usingRecursiveComparison().isEqualTo(expectedResponse);
    }

    @Test
    void 인증_헤더_정보가_유효하지_않을때_401을_응답한다() {
        String bearerAccessToken = authorizationForBearerAccessToken(user) + "somethingWrong";

        // when
        final ExtractableResponse<Response> response = RestAssured.given().log().all()
                .when().header("Authorization", bearerAccessToken)
                .get("/users")
                .then()
                .log().all()
                .extract();

        // then
        final int actualStatusCode = response.statusCode();
        final int expectedStatusCode = HttpStatus.UNAUTHORIZED.value();
        final APIErrorResponse actualResponse = response.as(APIErrorResponse.class);
        final APIErrorResponse expectedResponse = new APIErrorResponse(FailMessage.UNAUTHORIZED_INVALID_TOKEN.getCode(), FailMessage.UNAUTHORIZED_INVALID_TOKEN.getMessage());

        assertThat(actualStatusCode).isEqualTo(expectedStatusCode);
        assertThat(actualResponse).usingRecursiveComparison().isEqualTo(expectedResponse);
    }
}
