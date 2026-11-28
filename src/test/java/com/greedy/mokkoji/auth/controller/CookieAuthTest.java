package com.greedy.mokkoji.auth.controller;

import com.greedy.mokkoji.api.auth.controller.argumentResolver.AuthCredential;
import com.greedy.mokkoji.api.auth.service.AuthCookieFactory;
import com.greedy.mokkoji.common.ControllerTest;
import com.greedy.mokkoji.common.fixture.Fixture;
import com.greedy.mokkoji.db.university.entity.University;
import com.greedy.mokkoji.db.user.entity.User;
import com.greedy.mokkoji.enums.auth.AuthRole;
import com.greedy.mokkoji.enums.message.FailMessage;
import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("NonAsciiCharacters")
@DisplayName("쿠키 인증")
class CookieAuthTest extends ControllerTest {

    private User user;

    @BeforeEach
    void prepareData() {
        final University university = universityRepository.save(Fixture.createUniversity());
        user = userRepository.save(Fixture.createUser(university));
    }

    private String accessTokenOf(final User target) {
        return jwtUtil.generateAccessToken(new AuthCredential(AuthRole.USER, target.getId()));
    }

    @Test
    void access_token_쿠키만으로_인증이_필요한_API가_200을_응답한다() {
        final ExtractableResponse<Response> response = RestAssured.given().log().all()
                .cookie(AuthCookieFactory.ACCESS_TOKEN_COOKIE, accessTokenOf(user))
                .when()
                .get(prefixUrl + "/users")
                .then().log().all()
                .extract();

        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
    }

    @Test
    void 헤더와_쿠키가_둘_다_있으면_헤더가_우선한다() {
        final User other = userRepository.save(Fixture.createAnotherUser(user.getUniversity()));

        final ExtractableResponse<Response> response = RestAssured.given().log().all()
                .header("Authorization", "Bearer " + accessTokenOf(user))
                .cookie(AuthCookieFactory.ACCESS_TOKEN_COOKIE, accessTokenOf(other))
                .when()
                .get(prefixUrl + "/users")
                .then().log().all()
                .extract();

        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.jsonPath().getString("data.name")).isEqualTo(user.getName());
    }

    @Test
    void 헤더와_쿠키가_둘_다_없으면_401을_응답한다() {
        final ExtractableResponse<Response> response = RestAssured.given().log().all()
                .when()
                .get(prefixUrl + "/users")
                .then().log().all()
                .extract();

        assertThat(response.statusCode()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
        assertThat(response.jsonPath().getInt("code"))
                .isEqualTo(FailMessage.UNAUTHORIZED_EMPTY_HEADER.getCode());
    }

    @Test
    void 빈_access_token_쿠키는_인증_정보가_없는_것으로_본다() {
        final ExtractableResponse<Response> response = RestAssured.given().log().all()
                .cookie(AuthCookieFactory.ACCESS_TOKEN_COOKIE, "")
                .when()
                .get(prefixUrl + "/users")
                .then().log().all()
                .extract();

        assertThat(response.statusCode()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
    }

    @Test
    void 비로그인_허용_경로는_쿠키가_없어도_통과한다() {
        final ExtractableResponse<Response> response = RestAssured.given().log().all()
                .when()
                .get(prefixUrl + "/clubs?page=1&size=10")
                .then().log().all()
                .extract();

        assertThat(response.statusCode()).isNotEqualTo(HttpStatus.UNAUTHORIZED.value());
    }

    @Test
    void CORS_preflight_요청은_인증을_요구하지_않는다() {
        final ExtractableResponse<Response> response = RestAssured.given().log().all()
                .header("Origin", "http://localhost:3000")
                .header("Access-Control-Request-Method", "GET")
                .when()
                .options(prefixUrl + "/users")
                .then().log().all()
                .extract();

        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
    }
}
