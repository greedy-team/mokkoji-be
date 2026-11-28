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
import io.restassured.http.ContentType;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("NonAsciiCharacters")
@DisplayName("CSRF Origin 검사")
class CsrfOriginTest extends ControllerTest {

    private static final String ALLOWED_ORIGIN = "http://localhost:3000";
    // CORS 는 허용되지만 CSRF 목록에는 없는 출처.
    // Spring CORS 필터가 먼저 막아버리면 CSRF 인터셉터까지 도달하지 못하므로,
    // 두 목록을 분리해 둔 효과 자체를 검증하려면 이런 출처여야 한다.
    private static final String DISALLOWED_ORIGIN = "https://mokkoji.vercel.app";

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
    void 쿠키_인증_상태에서_허용되지_않은_Origin의_PATCH는_403을_응답한다() {
        final ExtractableResponse<Response> response = RestAssured.given().log().all()
                .cookie(AuthCookieFactory.ACCESS_TOKEN_COOKIE, accessToken())
                .header("Origin", DISALLOWED_ORIGIN)
                .contentType(ContentType.JSON)
                .body("{\"name\":\"바뀐이름\"}")
                .when()
                .patch(prefixUrl + "/users")
                .then().log().all()
                .extract();

        assertThat(response.statusCode()).isEqualTo(HttpStatus.FORBIDDEN.value());
        assertThat(response.jsonPath().getInt("code"))
                .isEqualTo(FailMessage.FORBIDDEN_INVALID_ORIGIN.getCode());
    }

    @Test
    void 같은_요청을_Bearer_헤더로_보내면_Origin이_달라도_통과한다() {
        final ExtractableResponse<Response> response = RestAssured.given().log().all()
                .header("Authorization", "Bearer " + accessToken())
                .header("Origin", DISALLOWED_ORIGIN)
                .contentType(ContentType.JSON)
                .body("{\"name\":\"바뀐이름\"}")
                .when()
                .patch(prefixUrl + "/users")
                .then().log().all()
                .extract();

        assertThat(response.statusCode()).isNotEqualTo(HttpStatus.FORBIDDEN.value());
    }

    @Test
    void 쿠키_인증_상태에서_허용된_Origin의_PATCH는_통과한다() {
        final ExtractableResponse<Response> response = RestAssured.given().log().all()
                .cookie(AuthCookieFactory.ACCESS_TOKEN_COOKIE, accessToken())
                .header("Origin", ALLOWED_ORIGIN)
                .contentType(ContentType.JSON)
                .body("{\"name\":\"바뀐이름\"}")
                .when()
                .patch(prefixUrl + "/users")
                .then().log().all()
                .extract();

        assertThat(response.statusCode()).isNotEqualTo(HttpStatus.FORBIDDEN.value());
    }

    @Test
    void 쿠키_인증_상태의_GET은_Origin을_검사하지_않는다() {
        final ExtractableResponse<Response> response = RestAssured.given().log().all()
                .cookie(AuthCookieFactory.ACCESS_TOKEN_COOKIE, accessToken())
                .header("Origin", DISALLOWED_ORIGIN)
                .when()
                .get(prefixUrl + "/users")
                .then().log().all()
                .extract();

        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
    }

    @Test
    void 쿠키_인증_상태에서_Origin_헤더가_없는_상태변경_요청은_403을_응답한다() {
        final ExtractableResponse<Response> response = RestAssured.given().log().all()
                .cookie(AuthCookieFactory.ACCESS_TOKEN_COOKIE, accessToken())
                .contentType(ContentType.JSON)
                .body("{\"name\":\"바뀐이름\"}")
                .when()
                .patch(prefixUrl + "/users")
                .then().log().all()
                .extract();

        assertThat(response.statusCode()).isEqualTo(HttpStatus.FORBIDDEN.value());
    }
}
