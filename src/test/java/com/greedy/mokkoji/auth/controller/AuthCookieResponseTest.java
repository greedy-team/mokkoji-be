package com.greedy.mokkoji.auth.controller;

import com.greedy.mokkoji.api.auth.controller.argumentResolver.AuthCredential;
import com.greedy.mokkoji.api.auth.service.AuthCookieFactory;
import com.greedy.mokkoji.common.ControllerTest;
import com.greedy.mokkoji.common.fixture.Fixture;
import com.greedy.mokkoji.db.university.entity.University;
import com.greedy.mokkoji.db.user.entity.User;
import com.greedy.mokkoji.enums.auth.AuthRole;
import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@SuppressWarnings("NonAsciiCharacters")
@DisplayName("인증 응답의 Set-Cookie")
class AuthCookieResponseTest extends ControllerTest {

    private User user;

    @BeforeEach
    void prepareData() {
        final University university = universityRepository.save(Fixture.createUniversity());
        user = userRepository.save(Fixture.createUser(university));
    }

    private List<String> setCookieHeaders(final ExtractableResponse<Response> response) {
        return response.headers().getValues("Set-Cookie");
    }

    @Test
    void 로그아웃하면_두_쿠키를_Max_Age_0으로_지운다() {
        final String accessToken = jwtUtil.generateAccessToken(
                new AuthCredential(AuthRole.USER, user.getId()));

        final ExtractableResponse<Response> response = RestAssured.given().log().all()
                .header("Authorization", "Bearer " + accessToken)
                .when()
                .post(prefixUrl + "/users/auth/logout")
                .then().log().all()
                .extract();

        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());

        final List<String> cookies = setCookieHeaders(response);
        assertThat(cookies).anySatisfy(cookie -> {
            assertThat(cookie).contains(AuthCookieFactory.ACCESS_TOKEN_COOKIE);
            assertThat(cookie).contains("Max-Age=0");
        });
        assertThat(cookies).anySatisfy(cookie -> {
            assertThat(cookie).contains(AuthCookieFactory.REFRESH_TOKEN_COOKIE);
            assertThat(cookie).contains("Max-Age=0");
        });
    }

    @Test
    void 로그아웃_쿠키는_HttpOnly와_Path를_발급_때와_같게_둔다() {
        final String accessToken = jwtUtil.generateAccessToken(
                new AuthCredential(AuthRole.USER, user.getId()));

        final ExtractableResponse<Response> response = RestAssured.given().log().all()
                .header("Authorization", "Bearer " + accessToken)
                .when()
                .post(prefixUrl + "/users/auth/logout")
                .then().log().all()
                .extract();

        assertThat(setCookieHeaders(response)).allSatisfy(cookie -> {
            assertThat(cookie).contains("HttpOnly");
            assertThat(cookie).contains("Path=/");
        });
    }

    @Test
    void refresh_token_쿠키만으로_갱신에_성공하고_새_access_token_쿠키를_내려준다() {
        final String refreshToken = jwtUtil.generateRefreshToken(
                new AuthCredential(AuthRole.USER, user.getId()));
        given(tokenService.getRefreshToken(AuthRole.USER, user.getId()))
                .willReturn(refreshToken);

        final ExtractableResponse<Response> response = RestAssured.given().log().all()
                .cookie(AuthCookieFactory.REFRESH_TOKEN_COOKIE, refreshToken)
                .when()
                .post(prefixUrl + "/users/auth/refresh")
                .then().log().all()
                .extract();

        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
        assertThat(setCookieHeaders(response)).anySatisfy(cookie ->
                assertThat(cookie).contains(AuthCookieFactory.ACCESS_TOKEN_COOKIE));
    }

    @Test
    void 헤더도_쿠키도_없는_갱신_요청은_401을_응답한다() {
        final ExtractableResponse<Response> response = RestAssured.given().log().all()
                .when()
                .post(prefixUrl + "/users/auth/refresh")
                .then().log().all()
                .extract();

        assertThat(response.statusCode()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
    }
}
