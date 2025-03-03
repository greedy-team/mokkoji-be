package com.greedy.mokkoji.user.controller;

import com.greedy.mokkoji.api.user.dto.request.UpdateUserInformationRequest;
import com.greedy.mokkoji.api.user.dto.resopnse.UserInformationResponse;
import com.greedy.mokkoji.common.ControllerTest;
import com.greedy.mokkoji.common.fixture.Fixture;
import com.greedy.mokkoji.db.user.entity.User;
import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class UserControllerTest extends ControllerTest {

    private User user;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        prepareData();
    }

    private void prepareData() {
        user = userRepository.save(Fixture.createUser());
    }

    @Test
    @DisplayName("사용자 정보를 가져올 수 있다.")
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
    @DisplayName("사용자 이메일을 업데이트 할 수 있다.")
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
    @DisplayName("사용자 이메일을 업데이트 할 수 있다.")
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
