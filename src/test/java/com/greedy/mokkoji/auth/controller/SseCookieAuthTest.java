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

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("NonAsciiCharacters")
@DisplayName("SSE 구독의 쿠키 인증과 CORS")
class SseCookieAuthTest extends ControllerTest {

    private static final String FRONT_ORIGIN = "http://localhost:3000";

    private User user;

    @BeforeEach
    void prepareData() {
        final University university = universityRepository.save(Fixture.createUniversity());
        user = userRepository.save(Fixture.createUser(university));
    }

    private String accessToken() {
        return jwtUtil.generateAccessToken(new AuthCredential(AuthRole.USER, user.getId()));
    }

    @Test
    void 쿠키만으로_SSE_구독에_성공한다() {
        final ExtractableResponse<Response> response = RestAssured.given()
                .cookie(AuthCookieFactory.ACCESS_TOKEN_COOKIE, accessToken())
                .header("Origin", FRONT_ORIGIN)
                .when()
                .get(prefixUrl + "/notifications/subscribe")
                .then()
                .extract();

        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
    }

    @Test
    void SSE_응답에_자격증명을_허용하는_CORS_헤더가_붙는다() {
        final ExtractableResponse<Response> response = RestAssured.given()
                .cookie(AuthCookieFactory.ACCESS_TOKEN_COOKIE, accessToken())
                .header("Origin", FRONT_ORIGIN)
                .when()
                .get(prefixUrl + "/notifications/subscribe")
                .then()
                .extract();

        // EventSource(withCredentials: true) 는 이 두 헤더가 없으면 CORS 실패로 보고,
        // 그 실패는 network error 로 분류되어 무한 재연결을 유발한다.
        assertThat(response.header("Access-Control-Allow-Origin")).isEqualTo(FRONT_ORIGIN);
        assertThat(response.header("Access-Control-Allow-Credentials")).isEqualTo("true");
    }

    @Test
    void SSE_응답의_Content_Type은_text_event_stream_이다() {
        final ExtractableResponse<Response> response = RestAssured.given()
                .cookie(AuthCookieFactory.ACCESS_TOKEN_COOKIE, accessToken())
                .header("Origin", FRONT_ORIGIN)
                .when()
                .get(prefixUrl + "/notifications/subscribe")
                .then()
                .extract();

        assertThat(response.contentType()).contains("text/event-stream");
    }

    @Test
    void 인증_정보가_없으면_SSE_구독은_401을_응답한다() {
        final ExtractableResponse<Response> response = RestAssured.given()
                .header("Origin", FRONT_ORIGIN)
                .when()
                .get(prefixUrl + "/notifications/subscribe")
                .then()
                .extract();

        assertThat(response.statusCode()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
    }
}
