package com.greedy.mokkoji.user;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.junit.jupiter.api.Test;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class UserControllerTest {

    @Value("${test.studentId}")
    private String studentId;

    @Value("${test.password}")
    private String password;

    @Test
    void 로그인_성공_테스트() {
        Map<String, String> params = new HashMap<>();
        params.put("studentId", studentId);
        params.put("password", password);

        ExtractableResponse<Response> response = RestAssured.given().log().ifValidationFails()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("https://www.mokkoji.o-r.kr/api/dev/users/auth/login")
                .then().log().all()
                .statusCode(200)
                .extract();

        String accessToken = response.body().jsonPath().getString("data.accessToken");
        String refreshToken = response.body().jsonPath().getString("data.refreshToken");

        assertThat(accessToken).isNotBlank();
        assertThat(refreshToken).isNotBlank();
    }

    //Todo: 로그인 실패 테스트

//    import io.restassured.RestAssured;
//import io.restassured.http.ContentType;
//import io.restassured.response.ExtractableResponse;
//import io.restassured.response.Response;
//import org.junit.jupiter.api.Test;
//
//import java.util.HashMap;
//import java.util.Map;
//
//import static org.assertj.core.api.Assertions.assertThat;
//
//    class AuthApiTest {
//
//        @Test
//        void 로그인_성공_테스트() {
//            Map<String, String> params = new HashMap<>();
//            params.put("studentId", "20231234");
//            params.put("password", "password");
//
//            ExtractableResponse<Response> response = RestAssured.given().log().all()
//                    .contentType(ContentType.JSON)
//                    .body(params)
//                    .when().post("/auth/login")
//                    .then().log().all()
//                    .statusCode(200)
//                    .extract();
//
//            String accessToken = response.body().jsonPath().getString("accessToken");
//            String refreshToken = response.body().jsonPath().getString("refreshToken");
//
//            assertThat(accessToken).isNotBlank();
//            assertThat(refreshToken).isNotBlank();
//        }
//
//        @Test
//        void 로그인_실패_테스트() {
//            Map<String, String> params = new HashMap<>();
//            params.put("studentId", "wrongId");
//            params.put("password", "wrongPassword");
//
//            RestAssured.given().log().all()
//                    .contentType(ContentType.JSON)
//                    .body(params)
//                    .when().post("/auth/login")
//                    .then().log().all()
//                    .statusCode(401);
//        }
//
//        @Test
//        void 토큰_검증_테스트() {
//            Map<String, String> params = new HashMap<>();
//            params.put("studentId", "20231234");
//            params.put("password", "password");
//
//            ExtractableResponse<Response> loginResponse = RestAssured.given().log().all()
//                    .contentType(ContentType.JSON)
//                    .body(params)
//                    .when().post("/auth/login")
//                    .then().log().all()
//                    .statusCode(200)
//                    .extract();
//
//            String accessToken = loginResponse.body().jsonPath().getString("accessToken");
//
//            ExtractableResponse<Response> checkResponse = RestAssured.given().log().all()
//                    .contentType(ContentType.JSON)
//                    .header("Authorization", "Bearer " + accessToken)
//                    .when().get("/auth/check")
//                    .then().log().all()
//                    .statusCode(200)
//                    .extract();
//
//            assertThat(checkResponse.body().jsonPath().getString("message")).isEqualTo("인증 성공");
//        }
//    }

}
