package com.greedy.mokkoji.user;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class UserControllerTest {

    //Todo: url 바꾸기

    @Value("${test.studentId}")
    private String studentId;

    @Value("${test.password}")
    private String password;

    @Test
    void 로그인_성공_테스트() {
        RestAssured.baseURI = "https://www.mokkoji.o-r.kr/api/dev";

        Map<String, String> params = new HashMap<>();
        params.put("studentId", studentId);
        params.put("password", password);

        ExtractableResponse<Response> response = RestAssured.given().log().ifValidationFails()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/users/auth/login")
                .then().log().all()
                .statusCode(200)
                .extract();

        String accessToken = response.body().jsonPath().getString("data.accessToken");
        String refreshToken = response.body().jsonPath().getString("data.refreshToken");

        assertThat(accessToken).isNotBlank();
        assertThat(refreshToken).isNotBlank();
    }

    @Test
    void 로그인_실패_테스트() {
        RestAssured.baseURI = "https://www.mokkoji.o-r.kr/api/dev";
        Map<String, String> params = new HashMap<>();
        params.put("studentId", "12345678");
        params.put("password", "password");

        ExtractableResponse<Response> response = RestAssured.given().log().ifValidationFails()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/users/auth/login")
                .then().log().all()
                .statusCode(500)
                .extract();
    }

    @Test
    void 로그인_후_리프레시_토큰_성공_테스트() {
        RestAssured.baseURI = "https://www.mokkoji.o-r.kr/api/dev";

        // 1. 로그인하여 refreshToken 획득
        Map<String, String> loginParams = new HashMap<>();
        loginParams.put("studentId", studentId);
        loginParams.put("password", password);

        ExtractableResponse<Response> loginResponse = RestAssured.given().log().ifValidationFails()
                .contentType(ContentType.JSON)
                .body(loginParams)
                .when().post("/users/auth/login")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract();

        String refreshToken = loginResponse.body().jsonPath().getString("data.refreshToken");

        // 2. 리프레시 토큰으로 새 액세스 토큰 요청
        ExtractableResponse<Response> refreshResponse = RestAssured.given().log().ifValidationFails()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + refreshToken)
                .when().post("/users/auth/refresh")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract();

        String newAccessToken = refreshResponse.body().jsonPath().getString("data.accessToken");

        assertThat(newAccessToken).isNotBlank();
    }

    @Test
    void 로그아웃_성공_테스트() {
        RestAssured.baseURI = "https://www.mokkoji.o-r.kr/api/dev";

        // 1. 로그인하여 refreshToken 획득
        Map<String, String> loginParams = new HashMap<>();
        loginParams.put("studentId", studentId);
        loginParams.put("password", password);

        ExtractableResponse<Response> loginResponse = RestAssured.given().log().ifValidationFails()
                .contentType(ContentType.JSON)
                .body(loginParams)
                .when().post("/users/auth/login")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract();

        String accessToken = loginResponse.body().jsonPath().getString("data.refreshToken");

        // 2. 로그아웃 확인
        ExtractableResponse<Response> response = RestAssured.given().log().ifValidationFails()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + accessToken)
                .when().post("users/auth/logout")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract();

        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
    }

}
