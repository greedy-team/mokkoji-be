package com.greedy.mokkoji.user.controller;

import com.greedy.mokkoji.api.user.dto.request.UpdateUserInformationRequest;
import com.greedy.mokkoji.api.user.dto.request.kakao.KakaoSocialLoginRequest;
import com.greedy.mokkoji.api.user.dto.resopnse.*;
import com.greedy.mokkoji.api.user.dto.resopnse.kakao.KakaoUserInfoResponse;
import com.greedy.mokkoji.api.user.service.kakao.KakaoSocialLoginService;
import com.greedy.mokkoji.common.ControllerTest;
import com.greedy.mokkoji.common.fixture.Fixture;
import com.greedy.mokkoji.db.club.entity.Club;
import com.greedy.mokkoji.db.university.entity.University;
import com.greedy.mokkoji.db.user.entity.User;
import com.greedy.mokkoji.enums.auth.AuthRole;
import com.greedy.mokkoji.enums.user.UserRole;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

public class UserControllerTest extends ControllerTest {

    @MockitoBean
    private KakaoSocialLoginService kakaoSocialLoginService;

    private User user;

    @BeforeEach
    void setUp() {
        favoriteRepository.deleteAll();
        recruitmentRepository.deleteAll();
        clubRepository.deleteAll();
        userRepository.deleteAll();
        universityRepository.deleteAll();
        prepareData();
    }

    private void prepareData() {
        user = userRepository.save(Fixture.createUser());
    }

    @Test
    @DisplayName("카카오 로그인 성공 여부를 검증한다.")
    void kakaoLoginSuccessful() {
        //given
        final String code = "authorizationCode";
        when(kakaoSocialLoginService.login(code))
                .thenReturn(new KakaoUserInfoResponse(user.getKakaoId(), null));

        final LoginResponse expected = LoginResponse.of("accessToken", "refreshToken", false);
        when(tokenService.generateToken(eq(AuthRole.USER), any(), anyBoolean())).thenReturn(expected);

        final KakaoSocialLoginRequest request = new KakaoSocialLoginRequest(code);

        //when
        ExtractableResponse<Response> response = RestAssured.given().log().ifValidationFails()
                .contentType(ContentType.JSON)
                .body(request)
                .when().post(prefixUrl + "/users/auth/kakao")
                .then().log().all()
                .statusCode(200)
                .extract();

        final LoginResponse actual = getDataFromResponse(response, LoginResponse.class);

        //then
        assertThat(actual).usingRecursiveComparison().isEqualTo(expected);
    }

    @Test
    @DisplayName("액세스 토큰 재발급 테스트")
    void refreshAccessToken() {
        // given
        final String bearerToken = authorizationForBearerRefreshToken(user);
        final String refreshToken = bearerToken.substring("bearer".length()).trim();
        when(tokenService.getRefreshToken(any())).thenReturn(refreshToken);

        // when
        ExtractableResponse<Response> response = RestAssured.given().log().ifValidationFails()
                .contentType(ContentType.JSON)
                .header("Authorization", bearerToken)
                .when().post(prefixUrl + "/users/auth/refresh")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract();

        final RefreshResponse actual = getDataFromResponse(response, RefreshResponse.class);

        //then
        assertThat(actual.accessToken()).isNotBlank();
    }

    @Test
    @DisplayName("로그아웃 성공 여부를 검증한다.")
    void logout() {
        // given
        doNothing().when(tokenService).deleteRefreshToken(any());

        // when
        ExtractableResponse<Response> response = RestAssured.given().log().ifValidationFails()
                .contentType(ContentType.JSON)
                .header("Authorization", authorizationForBearerAccessToken(user))
                .when().post(prefixUrl + "/users/auth/logout")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract();

        //then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
    }

    @Test
    @DisplayName("사용자 정보를 조회 성공 여부를 검증한다.")
    void getUserInfo() {
        // given
        final String authorizationForBearer = authorizationForBearerAccessToken(user);
        final UserInformationResponse expected = UserInformationResponse.of(user);

        // when
        final ExtractableResponse<Response> response = RestAssured.given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header("Authorization", authorizationForBearer)
                .when().get(prefixUrl + "/users")
                .then().log().all()
                .extract();

        final int statusCode = response.statusCode();
        final UserInformationResponse actual = getDataFromResponse(response, UserInformationResponse.class);

        // then
        assertThat(statusCode).isEqualTo(HttpStatus.OK.value());
        assertThat(expected).usingRecursiveComparison().isEqualTo(actual);
    }

    @Test
    @DisplayName("사용자 이메일 업데이트 성공 여부를 검증한다.")
    void updateUserInfo() {
        // given
        final String authorizationForBearer = authorizationForBearerAccessToken(user);
        final String updatedEmail = "updatedEmail@test.com";
        final UpdateUserInformationRequest updateUserInformationRequest = new UpdateUserInformationRequest(updatedEmail, null);

        // when
        final ExtractableResponse<Response> response = RestAssured.given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header("Authorization", authorizationForBearer)
                .body(updateUserInformationRequest)
                .when().patch(prefixUrl + "/users")
                .then().log().all()
                .extract();

        final int statusCode = response.statusCode();

        // then
        assertThat(statusCode).isEqualTo(HttpStatus.OK.value());
    }

    @Test
    @DisplayName("유효하지 않은 이메일 형식일 경우를 검증한다.")
    void updateUserInfoWithIncorrectEmail() {
        // given
        final String authorizationForBearer = authorizationForBearerAccessToken(user);
        final String invalidUpdatedEmail = "updatedEmail.com";
        final UpdateUserInformationRequest updateUserInformationRequest = new UpdateUserInformationRequest(invalidUpdatedEmail, null);

        // when
        final ExtractableResponse<Response> response = RestAssured.given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header("Authorization", authorizationForBearer)
                .body(updateUserInformationRequest)
                .when().patch(prefixUrl + "/users")
                .then().log().all()
                .extract();

        final int statusCode = response.statusCode();

        // then
        assertThat(statusCode).isEqualTo(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    @DisplayName("사용자 역할 조회 성공 여부를 검증한다.")
    void getUserRole() {
        // given
        final String authorizationForBearer = authorizationForBearerAccessToken(user);
        final UserRole expected = user.getRole();

        // when
        final ExtractableResponse<Response> response = RestAssured.given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header("Authorization", authorizationForBearer)
                .when().get(prefixUrl + "/users/roles")
                .then().log().all()
                .extract();

        final int statusCode = response.statusCode();
        final UserRoleResponse actual = getDataFromResponse(response, UserRoleResponse.class);

        // then
        assertThat(statusCode).isEqualTo(HttpStatus.OK.value());
        assertThat(expected).isEqualTo(actual.role());
    }

    @Test
    @DisplayName("사용자가 회장인 동아리 조회 성공 여부를 검증한다.")
    void getUserManageClubs() {
        // given
        final String authorizationForBearer = authorizationForBearerAccessToken(user);

        final University university = universityRepository.save(Fixture.createUniversity());
        final Club club = clubRepository.save(Fixture.createClub(university, user));
        final UserManageClubsResponse expected = new UserManageClubsResponse(
                List.of(new UserManageClubResponse(club.getId(), club.getName()))
        );

        // when
        final ExtractableResponse<Response> response = RestAssured.given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header("Authorization", authorizationForBearer)
                .when().get(prefixUrl + "/users/manage/clubs")
                .then().log().all()
                .extract();

        final int statusCode = response.statusCode();
        final UserManageClubsResponse actual = getDataFromResponse(response, UserManageClubsResponse.class);

        // then
        assertThat(statusCode).isEqualTo(HttpStatus.OK.value());
        assertThat(expected).usingRecursiveComparison().isEqualTo(actual);
    }
}
