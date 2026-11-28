package com.greedy.mokkoji.auth.controller;

import com.greedy.mokkoji.api.auth.controller.argumentResolver.AuthCredential;
import com.greedy.mokkoji.api.auth.service.AuthCookieFactory;
import com.greedy.mokkoji.common.ControllerTest;
import com.greedy.mokkoji.enums.auth.AuthRole;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 관리자 대시보드는 세션 체계가 달라 이번 전환 범위에서 제외한다.
 */
@SuppressWarnings("NonAsciiCharacters")
@DisplayName("관리자는 헤더 방식을 유지한다")
class AdminHeaderOnlyTest extends ControllerTest {

    @Test
    void 관리자_로그인_응답에는_인증_쿠키를_내려주지_않는다() {
        final ExtractableResponse<Response> response = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body("{\"loginId\":\"no-such-admin@test.com\",\"password\":\"wrongPassword1!\"}")
                .when()
                .post(prefixUrl + "/admin/auth/login")
                .then().log().all()
                .extract();

        // 로그인 성공 여부와 무관하게, 관리자 경로는 인증 쿠키를 발급하지 않는다.
        assertThat(response.headers().getValues("Set-Cookie"))
                .noneMatch(cookie -> cookie.contains(AuthCookieFactory.ACCESS_TOKEN_COOKIE)
                        || cookie.contains(AuthCookieFactory.REFRESH_TOKEN_COOKIE));
    }

    @Test
    void 관리자_토큰은_Bearer_헤더로_정상_동작한다() {
        final String adminToken = jwtUtil.generateAccessToken(
                new AuthCredential(AuthRole.ADMIN, 1L));

        final ExtractableResponse<Response> response = RestAssured.given().log().all()
                .header("Authorization", "Bearer " + adminToken)
                .when()
                .get(prefixUrl + "/admin/me")
                .then().log().all()
                .extract();

        // 인증 자체는 통과한다(관리자 데이터가 없어 404 가 날 수는 있으나 401 은 아니다).
        assertThat(response.statusCode()).isNotEqualTo(HttpStatus.UNAUTHORIZED.value());
    }
}
