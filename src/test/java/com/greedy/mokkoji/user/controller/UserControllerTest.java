package com.greedy.mokkoji.user.controller;

import com.greedy.mokkoji.api.user.dto.request.UpdateUserInformationRequest;
import com.greedy.mokkoji.api.user.dto.resopnse.LoginResponse;
import com.greedy.mokkoji.api.user.dto.resopnse.RefreshResponse;
import com.greedy.mokkoji.api.user.dto.resopnse.UserInformationResponse;
import com.greedy.mokkoji.common.ControllerTest;
import com.greedy.mokkoji.common.fixture.Fixture;
import com.greedy.mokkoji.db.user.entity.User;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class UserControllerTest extends ControllerTest {

    @Value("${test.studentId}")
    private String studentId;

    @Value("${test.password}")
    private String password;

    private User user;

    @BeforeEach
    void setUp() {
        favoriteRepository.deleteAll();
        userRepository.deleteAll();
        recruitmentRepository.deleteAll();
        clubRepository.deleteAll();
        prepareData();
    }

    private void prepareData() {
        user = userRepository.save(Fixture.createUser());
    }

    @Test
    @DisplayName("로그인 성공 테스트")
    void loginSuccessful() {
        //given
        Map<String, String> params = new HashMap<>();
        params.put("studentId", studentId);
        params.put("password", password);

        //when
        ExtractableResponse<Response> response = RestAssured.given().log().ifValidationFails()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post(prefixUrl + "/users/auth/login")
                .then().log().all()
                .statusCode(200)
                .extract();
        final LoginResponse actual = getDataFromResponse(response, LoginResponse.class);

        //then
        assertThat(actual.accessToken()).isNotBlank();
        assertThat(actual.refreshToken()).isNotBlank();
    }

    @Test
    @DisplayName("로그인 실패 테스트")
    void loginFailed() {
        //given
        Map<String, String> params = new HashMap<>();
        params.put("studentId", "12345678");
        params.put("password", "password");

        //when & then
        ExtractableResponse<Response> response = RestAssured.given().log().ifValidationFails()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post(prefixUrl + "/users/auth/login")
                .then().log().all()
                .statusCode(500)
                .extract();
    }

    @Test
    @DisplayName("액세스 토큰 재발급 테스트")
    void refreshAccessToken() {
        // given
        // 1. 로그인하여 refreshToken 획득
        Map<String, String> loginParams = new HashMap<>();
        loginParams.put("studentId", studentId);
        loginParams.put("password", password);

        ExtractableResponse<Response> loginResponse = RestAssured.given().log().ifValidationFails()
                .contentType(ContentType.JSON)
                .body(loginParams)
                .when().post(prefixUrl + "/users/auth/login")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract();

        final LoginResponse loginResponseDto = getDataFromResponse(loginResponse, LoginResponse.class);

        // when
        // 2. 리프레시 토큰으로 새 액세스 토큰 요청
        ExtractableResponse<Response> refreshResponse = RestAssured.given().log().ifValidationFails()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + loginResponseDto.refreshToken())
                .when().post(prefixUrl + "/users/auth/refresh")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract();

        final RefreshResponse actual = getDataFromResponse(refreshResponse, RefreshResponse.class);

        //then
        assertThat(actual.accessToken()).isNotBlank();
    }

    @Test
    @DisplayName("로그아웃 성공 테스트")
    void logout() {
        // given
        // 1. 로그인하여 refreshToken 획득
        Map<String, String> loginParams = new HashMap<>();
        loginParams.put("studentId", studentId);
        loginParams.put("password", password);

        ExtractableResponse<Response> loginResponse = RestAssured.given().log().ifValidationFails()
                .contentType(ContentType.JSON)
                .body(loginParams)
                .when().post(prefixUrl + "/users/auth/login")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract();

        final LoginResponse loginResponseDto = getDataFromResponse(loginResponse, LoginResponse.class);

        // when
        // 2. 로그아웃
        ExtractableResponse<Response> response = RestAssured.given().log().ifValidationFails()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + loginResponseDto.accessToken())
                .when().post(prefixUrl +"/users/auth/logout")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract();

        //then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
    }

    @Test
    @DisplayName("사용자 정보를 가져오기 성공 테스트")
    void getUserInfo() {
        // given
        String authorizationForBearer = authorizationForBearer(user);

        // when
        final ExtractableResponse<Response> response = RestAssured.given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header("Authorization", authorizationForBearer)
                .when().get(prefixUrl + "/users")
                .then().log().all()
                .extract();

        final int statusCode = response.statusCode();
        final UserInformationResponse actual = getDataFromResponse(response, UserInformationResponse.class);
        final UserInformationResponse expected = UserInformationResponse.of(user);

        // then
        assertThat(statusCode).isEqualTo(HttpStatus.OK.value());
        assertThat(expected).usingRecursiveComparison().isEqualTo(actual);
    }

    @Test
    @DisplayName("사용자 이메일 업데이트 성공 테스트")
    void updateUserInfo() {
        // given
        String authorizationForBearer = authorizationForBearer(user);
        String updatedEmail = "updatedEmail@test.com";
        UpdateUserInformationRequest updateUserInformationRequest = new UpdateUserInformationRequest(updatedEmail);

        // when
        final ExtractableResponse<Response> response = RestAssured.given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header("Authorization", authorizationForBearer)
                .body(updateUserInformationRequest)
                .when().put(prefixUrl + "/users")
                .then().log().all()
                .extract();

        final int statusCode = response.statusCode();

        // then
        assertThat(statusCode).isEqualTo(HttpStatus.OK.value());
    }

    @Test
    @DisplayName("사용자 이메일 업데이트 실패 테스트")
    void updateUserInfoWithIncorrectEmail() {
        // given
        String authorizationForBearer = authorizationForBearer(user);
        String updatedEmail = "updatedEmailtest.com";
        Map<String, String> body = new HashMap<>();
        body.put("email", updatedEmail);

        // when
        final ExtractableResponse<Response> response = RestAssured.given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header("Authorization", authorizationForBearer)
                .body(body)
                .when().put(prefixUrl + "/users")
                .then().log().all()
                .extract();

        final int statusCode = response.statusCode();

        // then
        assertThat(statusCode).isEqualTo(HttpStatus.BAD_REQUEST.value());
    }
}
