package com.greedy.mokkoji.admin.controller;

import com.greedy.mokkoji.api.admin.dto.request.AdminLoginRequest;
import com.greedy.mokkoji.api.admin.dto.response.AdminLoginResponse;
import com.greedy.mokkoji.api.auth.dto.TokenPair;
import com.greedy.mokkoji.common.ControllerTest;
import com.greedy.mokkoji.db.admin.entity.Admin;
import com.greedy.mokkoji.db.admin.repository.AdminRepository;
import com.greedy.mokkoji.db.university.entity.University;
import com.greedy.mokkoji.enums.admin.AdminRole;
import com.greedy.mokkoji.enums.auth.AuthRole;
import com.greedy.mokkoji.enums.university.UniversityCode;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

public class AdminControllerTest extends ControllerTest {

    private static final String RAW_PASSWORD = "rawPassword123!";

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @AfterEach
    void cleanUpAdmins() {
        adminRepository.deleteAll();
    }

    @Test
    @DisplayName("총동연 관리자 로그인 시 토큰과 함께 권한, 소속 학교 코드를 반환한다.")
    void loginUniversityAdmin() {
        //given
        final University university = universityRepository.save(
                University.builder()
                        .name("세종대학교")
                        .code(UniversityCode.SEJONG)
                        .logo("세종대_로고")
                        .build()
        );
        final Admin admin = adminRepository.save(
                Admin.builder()
                        .university(university)
                        .loginId("univ-admin@sejong.ac.kr")
                        .password(passwordEncoder.encode(RAW_PASSWORD))
                        .role(AdminRole.UNIVERSITY_ADMIN)
                        .build()
        );
        when(tokenService.issueTokens(eq(AuthRole.ADMIN), any())).thenReturn(new TokenPair("accessToken", "refreshToken"));

        final AdminLoginResponse expected = AdminLoginResponse.of(
                "accessToken", "refreshToken", AdminRole.UNIVERSITY_ADMIN, UniversityCode.SEJONG
        );

        //when
        final ExtractableResponse<Response> response = RestAssured.given().log().ifValidationFails()
                .contentType(ContentType.JSON)
                .body(new AdminLoginRequest(admin.getLoginId(), RAW_PASSWORD))
                .when().post(prefixUrl + "/admin/auth/login")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract();

        //then
        assertThat(getDataFromResponse(response, AdminLoginResponse.class))
                .usingRecursiveComparison().isEqualTo(expected);
    }

    @Test
    @DisplayName("모꼬지 관리자가 로그인 시 학교 코드는 null로 반환된다.")
    void loginMokkojiAdmin() {
        //given
        final Admin admin = adminRepository.save(
                Admin.builder()
                        .loginId("admin@konkuk.ac.kr")
                        .password(passwordEncoder.encode(RAW_PASSWORD))
                        .role(AdminRole.MOKKOJI_ADMIN)
                        .build()
        );
        when(tokenService.issueTokens(eq(AuthRole.ADMIN), any())).thenReturn(new TokenPair("accessToken", "refreshToken"));

        final AdminLoginResponse expected = AdminLoginResponse.of(
                "accessToken", "refreshToken", AdminRole.MOKKOJI_ADMIN, null
        );

        //when
        final ExtractableResponse<Response> response = RestAssured.given().log().ifValidationFails()
                .contentType(ContentType.JSON)
                .body(new AdminLoginRequest(admin.getLoginId(), RAW_PASSWORD))
                .when().post(prefixUrl + "/admin/auth/login")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract();

        //then
        assertThat(getDataFromResponse(response, AdminLoginResponse.class))
                .usingRecursiveComparison().isEqualTo(expected);
    }

    @Test
    @DisplayName("비밀번호가 틀리면 401 응답을 반환한다.")
    void loginWrongPassword() {
        //given
        adminRepository.save(
                Admin.builder()
                        .loginId("admin@konkuk.ac.kr")
                        .password(passwordEncoder.encode(RAW_PASSWORD))
                        .role(AdminRole.MOKKOJI_ADMIN)
                        .build()
        );

        //when & then
        RestAssured.given().log().ifValidationFails()
                .contentType(ContentType.JSON)
                .body(new AdminLoginRequest("admin@konkuk.ac.kr", "wrongPassword"))
                .when().post(prefixUrl + "/admin/auth/login")
                .then().log().all()
                .statusCode(HttpStatus.UNAUTHORIZED.value());
    }
}
