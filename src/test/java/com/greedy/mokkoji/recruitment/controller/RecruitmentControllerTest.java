package com.greedy.mokkoji.recruitment.controller;

import com.greedy.mokkoji.api.recruitment.dto.request.CreateRecruitmentRequest;
import com.greedy.mokkoji.api.recruitment.dto.request.UpdateRecruitmentRequest;
import com.greedy.mokkoji.api.recruitment.dto.response.allRecruitmentOfClub.RecruitmentOfClubResponse;
import com.greedy.mokkoji.api.recruitment.dto.response.recentRecruitment.RecentRecruitmentOfClubResponse;
import com.greedy.mokkoji.common.ControllerTest;
import com.greedy.mokkoji.common.fixture.Fixture;
import com.greedy.mokkoji.common.response.APIErrorResponse;
import com.greedy.mokkoji.db.club.entity.Club;
import com.greedy.mokkoji.db.recruitment.entity.Recruitment;
import com.greedy.mokkoji.db.university.entity.University;
import com.greedy.mokkoji.db.user.entity.User;
import com.greedy.mokkoji.enums.message.FailMessage;
import com.greedy.mokkoji.enums.user.UserRole;
import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Stream;

import static com.greedy.mokkoji.common.fixture.Fixture.FIXTURE_RECRUITMENT_IMAGE_NAME;
import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

public class RecruitmentControllerTest extends ControllerTest {

    private University university;
    private Club club;
    private Recruitment recruitment;

    static Stream<UserRole> allowedRoles() {
        return Stream.of(UserRole.CLUB_MASTER);
    }

    static Stream<UserRole> forbiddenRoles() {
        return Stream.of(UserRole.NORMAL);
    }

    @BeforeEach
    void setUp() {
        university = universityRepository.save(Fixture.createUniversity());
    }

    @ParameterizedTest
    @MethodSource("allowedRoles")
    @DisplayName("권한을 가진 관리자는 모집글을 생성할 수 있다.")
    void createRecruitment_allowedRoles_success(UserRole role) {
        //given
        User adminUser = userRepository.save(Fixture.createUserWithRole(university,role));
        club = clubRepository.save(Fixture.createClub(university, adminUser));
        recruitment = recruitmentRepository.save(Fixture.createRecruitment(club));
        String authorizationForBearer = authorizationForBearerAccessToken(adminUser);

        final CreateRecruitmentRequest request = new CreateRecruitmentRequest(
                recruitment.getTitle(),
                FIXTURE_RECRUITMENT_IMAGE_NAME,
                recruitment.getContent(),
                recruitment.getRecruitStart(),
                recruitment.getRecruitEnd(),
                recruitment.getRecruitForm(),
                recruitment.isAlwaysRecruiting()
        );

        //when
        final ExtractableResponse<Response> response = given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header("Authorization", authorizationForBearer)
                .body(request)
                .when().post(prefixUrl + "/recruitments/{clubId}", club.getId())
                .then().log().all()
                .extract();

        //then
        final int statusCode = response.statusCode();
        Recruitment createRecruitment = recruitmentRepository.findAllByClubId(club.getId()).get(0);
        assertThat(statusCode).isEqualTo(HttpStatus.CREATED.value());
        assertThat(createRecruitment.getTitle()).isEqualTo(request.title());
        assertThat(createRecruitment.getContent()).isEqualTo(request.content());
        assertThat(createRecruitment.getRecruitStart()).isEqualTo(request.recruitStart());
        assertThat(createRecruitment.getRecruitEnd()).isEqualTo(request.recruitEnd());
        assertThat(createRecruitment.isAlwaysRecruiting()).isEqualTo(request.isAlwaysRecruiting());
    }

    @ParameterizedTest
    @MethodSource("forbiddenRoles")
    @DisplayName("허용되지 않은 권한을 가진 일반 사용자가 모집글 생성 시 403을 반환한다.")
    void createRecruitment_forbiddenRoles_403(UserRole role) {
        // given
        club = clubRepository.save(Fixture.createClub(university));
        recruitment = recruitmentRepository.save(Fixture.createRecruitment(club));
        User normalUser = userRepository.save(Fixture.createUserWithRole(university,role));
        String authorizationForBearer = authorizationForBearerAccessToken(normalUser);

        final CreateRecruitmentRequest request = new CreateRecruitmentRequest(
                recruitment.getTitle(),
                FIXTURE_RECRUITMENT_IMAGE_NAME,
                recruitment.getContent(),
                recruitment.getRecruitStart(),
                recruitment.getRecruitEnd(),
                recruitment.getRecruitForm(),
                recruitment.isAlwaysRecruiting()
        );

        // when
        ExtractableResponse<Response> response = RestAssured.given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header("Authorization", authorizationForBearer)
                .body(request)
                .when().post(prefixUrl + "/recruitments/{clubId}", club.getId())
                .then().log().all()
                .extract();

        // then
        final int actualStatusCode = response.statusCode();
        final APIErrorResponse actualResponse = response.as(APIErrorResponse.class);
        final APIErrorResponse expectedResponse = new APIErrorResponse(
                FailMessage.FORBIDDEN_MANAGE_CLUB.getCode(),
                FailMessage.FORBIDDEN_MANAGE_CLUB.getMessage()
        );

        assertThat(actualStatusCode).isEqualTo(HttpStatus.FORBIDDEN.value());
        assertThat(actualResponse).usingRecursiveComparison().isEqualTo(expectedResponse);
    }

    @Test
    @DisplayName("토큰 없이 모집글 생성 요청 시 401을 반환한다.")
    void createRecruitment_withoutToken_shouldReturn401() {
        // given
        club = clubRepository.save(Fixture.createClub(university));
        recruitment = recruitmentRepository.save(Fixture.createRecruitment(club));

        final CreateRecruitmentRequest request = new CreateRecruitmentRequest(
                recruitment.getTitle(),
                FIXTURE_RECRUITMENT_IMAGE_NAME,
                recruitment.getContent(),
                recruitment.getRecruitStart(),
                recruitment.getRecruitEnd(),
                recruitment.getRecruitForm(),
                recruitment.isAlwaysRecruiting()
        );

        // when
        ExtractableResponse<Response> response = RestAssured.given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(request)
                .when().post(prefixUrl + "/recruitments/{clubId}", club.getId())
                .then().log().all()
                .extract();

        // then
        final int actualStatusCode = response.statusCode();
        final APIErrorResponse actualResponse = response.as(APIErrorResponse.class);
        final APIErrorResponse expectedResponse = new APIErrorResponse(
                FailMessage.UNAUTHORIZED.getCode(),
                FailMessage.UNAUTHORIZED.getMessage()
        );

        assertThat(actualStatusCode).isEqualTo(HttpStatus.UNAUTHORIZED.value());
        assertThat(actualResponse).usingRecursiveComparison().isEqualTo(expectedResponse);
    }

    @ParameterizedTest
    @MethodSource("allowedRoles")
    @DisplayName("권한을 가진 관리자는 모집글을 수정할 수 있다.")
    void updateRecruitment_allowedRoles_success(UserRole role) {
        // given
        User adminUser = userRepository.save(Fixture.createUserWithRole(university,role));
        club = clubRepository.save(Fixture.createClub(university, adminUser));
        recruitment = recruitmentRepository.save(Fixture.createRecruitment(club));
        String authorizationForBearer = authorizationForBearerAccessToken(adminUser);

        final UpdateRecruitmentRequest request = new UpdateRecruitmentRequest(
                "수정된 모집글 제목",
                List.of("수정된 모집글 이미지.png"),
                "수정된 모집글 내용",
                recruitment.getRecruitStart().plusDays(1),
                recruitment.getRecruitEnd().plusDays(1),
                "수정된 모집글 링크",
                false
        );

        // when
        ExtractableResponse<Response> response = RestAssured.given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header("Authorization", authorizationForBearer)
                .body(request)
                .when().patch(prefixUrl + "/recruitments/{recruitmentId}", recruitment.getId())
                .then().log().all()
                .extract();

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());

        Recruitment updatedRecruitment = recruitmentRepository.findById(recruitment.getId()).orElseThrow();
        assertThat(updatedRecruitment.getTitle()).isEqualTo("수정된 모집글 제목");
        assertThat(updatedRecruitment.getContent()).isEqualTo("수정된 모집글 내용");
        assertThat(updatedRecruitment.getRecruitStart()).isEqualTo(recruitment.getRecruitStart().plusDays(1));
        assertThat(updatedRecruitment.getRecruitEnd()).isEqualTo(recruitment.getRecruitEnd().plusDays(1));
        assertThat(updatedRecruitment.getRecruitForm()).isEqualTo("수정된 모집글 링크");
    }

    @ParameterizedTest
    @MethodSource("forbiddenRoles")
    @DisplayName("허용되지 않은 권한을 가진 일반 사용자가 모집글 수정 시 403을 반환한다.")
    void updateRecruitment_forbiddenRoles_403(UserRole role) {
        // given
        club = clubRepository.save(Fixture.createClub(university));
        recruitment = recruitmentRepository.save(Fixture.createRecruitment(club));
        User normalUser = userRepository.save(Fixture.createUserWithRole(university,role));
        String authorizationForBearer = authorizationForBearerAccessToken(normalUser);

        final UpdateRecruitmentRequest request = new UpdateRecruitmentRequest(
                "수정된 모집글 제목",
                List.of("수정된 모집글 이미지.png"),
                "수정된 모집글 내용",
                recruitment.getRecruitStart().plusDays(1),
                recruitment.getRecruitEnd().plusDays(1),
                "수정된 모집글 링크",
                false
        );

        // when
        ExtractableResponse<Response> response = RestAssured.given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header("Authorization", authorizationForBearer)
                .body(request)
                .when().patch(prefixUrl + "/recruitments/{recruitmentId}", recruitment.getId())
                .then().log().all()
                .extract();

        // then
        final int actualStatusCode = response.statusCode();
        final APIErrorResponse actualResponse = response.as(APIErrorResponse.class);
        final APIErrorResponse expectedResponse = new APIErrorResponse(
                FailMessage.FORBIDDEN_MANAGE_CLUB.getCode(),
                FailMessage.FORBIDDEN_MANAGE_CLUB.getMessage()
        );

        assertThat(actualStatusCode).isEqualTo(HttpStatus.FORBIDDEN.value());
        assertThat(actualResponse).usingRecursiveComparison().isEqualTo(expectedResponse);
    }

    @ParameterizedTest
    @MethodSource("allowedRoles")
    @DisplayName("권한을 가진 관리자는 모집글을 삭제할 수 있다.")
    void deleteRecruitment_allowedRoles_success(UserRole role) {
        // given
        User adminUser = userRepository.save(Fixture.createUserWithRole(university,role));
        club = clubRepository.save(Fixture.createClub(university, adminUser));
        recruitment = recruitmentRepository.save(Fixture.createRecruitment(club));
        String authorizationForBearer = authorizationForBearerAccessToken(adminUser);

        //when
        ExtractableResponse<Response> response = RestAssured.given().log().all()
                .header("Authorization", authorizationForBearer)
                .when().delete(prefixUrl + "/recruitments/{recruitmentId}", recruitment.getId())
                .then().log().all()
                .extract();

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
        assertThat(recruitmentRepository.findById(recruitment.getId())).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("forbiddenRoles")
    @DisplayName("허용되지 않은 권한을 가진 일반 사용자가 모집글 삭제 시 403을 반환한다.")
    void deleteRecruitment_forbiddenRoles_403(UserRole role) {
        // given
        club = clubRepository.save(Fixture.createClub(university));
        recruitment = recruitmentRepository.save(Fixture.createRecruitment(club));
        User normalUser = userRepository.save(Fixture.createUserWithRole(university,role));
        String authorizationForBearer = authorizationForBearerAccessToken(normalUser);

        //when
        ExtractableResponse<Response> response = RestAssured.given().log().all()
                .header("Authorization", authorizationForBearer)
                .when().delete(prefixUrl + "/recruitments/{recruitmentId}", recruitment.getId())
                .then().log().all()
                .extract();

        // then
        final int actualStatusCode = response.statusCode();
        final APIErrorResponse actualResponse = response.as(APIErrorResponse.class);
        final APIErrorResponse expectedResponse = new APIErrorResponse(
                FailMessage.FORBIDDEN_MANAGE_CLUB.getCode(),
                FailMessage.FORBIDDEN_MANAGE_CLUB.getMessage()
        );

        assertThat(actualStatusCode).isEqualTo(HttpStatus.FORBIDDEN.value());
        assertThat(actualResponse).usingRecursiveComparison().isEqualTo(expectedResponse);
    }

    @Test
    @DisplayName("특정 동아리의 모든 모집글을 최신순으로 조회한다.")
    void getAllRecruitmentOfClub_shouldReturnNewestFirst() {
        // given
        club = clubRepository.save(Fixture.createClub(university));
        recruitment = recruitmentRepository.save(Fixture.createRecruitment(club));
        Recruitment olderRecruitment = recruitmentRepository.save(Fixture.createOrderRecruitment(club));
        Recruitment newerRecruitment = recruitmentRepository.save(Fixture.createNewerRecruitment(club));

        // when
        ExtractableResponse<Response> response = RestAssured.given().log().all()
                .when().get(prefixUrl + "/recruitments/club/{clubId}", club.getId())
                .then().log().all()
                .extract();

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());

        List<RecruitmentOfClubResponse> recruitments =
                response.jsonPath().getList("data.recruitments", RecruitmentOfClubResponse.class);

        assertThat(recruitments).isNotEmpty();

        RecruitmentOfClubResponse first = recruitments.get(0);
        assertThat(first.id()).isEqualTo(newerRecruitment.getId());
        assertThat(first.isAlwaysRecruiting()).isFalse();

        assertThat(recruitments.stream().anyMatch(
                recruitmentOfClubResponse -> recruitmentOfClubResponse.id().equals(recruitment.getId()))).isTrue();
        assertThat(recruitments.stream().anyMatch(
                recruitmentOfClubResponse -> recruitmentOfClubResponse.id().equals(olderRecruitment.getId()))).isTrue();
    }

    @Test
    @DisplayName("동아리의 모집글이 없는 경우 모집글 관련 필드는 null 혹은 빈 값으로 응답한다.")
    void getRecentRecruitmentOfClub_whenNoRecruitment_shouldReturnNullRecruitmentFields() {
        // given
        club = clubRepository.save(Fixture.createClub(university));
        User normalUser = userRepository.save(Fixture.createUserWithRole(university,UserRole.NORMAL));
        String authorizationForBearer = authorizationForBearerAccessToken(normalUser);

        // when
        ExtractableResponse<Response> response = RestAssured.given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header("Authorization", authorizationForBearer)
                .when().get(prefixUrl + "/recruitments/club/recent/{clubId}", club.getId())
                .then().log().all()
                .extract();

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());

        RecentRecruitmentOfClubResponse recentRecruitmentOfClubResponse = getDataFromResponse(response, RecentRecruitmentOfClubResponse.class);

        assertThat(recentRecruitmentOfClubResponse.clubId()).isEqualTo(club.getId());
        assertThat(recentRecruitmentOfClubResponse.clubName()).isEqualTo(club.getName());

        assertThat(recentRecruitmentOfClubResponse.id()).isNull();
        assertThat(recentRecruitmentOfClubResponse.title()).isNull();
        assertThat(recentRecruitmentOfClubResponse.content()).isNull();
        assertThat(recentRecruitmentOfClubResponse.recruitStart()).isNull();
        assertThat(recentRecruitmentOfClubResponse.recruitEnd()).isNull();
        assertThat(recentRecruitmentOfClubResponse.status()).isNull();
        assertThat(recentRecruitmentOfClubResponse.createdAt()).isNull();
        assertThat(recentRecruitmentOfClubResponse.recruitForm()).isNull();
        assertThat(recentRecruitmentOfClubResponse.isAlwaysRecruiting()).isFalse();
        assertThat(recentRecruitmentOfClubResponse.imageUrls()).isEmpty();
    }

    @Test
    @DisplayName("동아리의 최신 모집글을 조회한다.")
    void getRecentRecruitmentOfClub() {
        // given
        club = clubRepository.save(Fixture.createClub(university));
        recruitment = recruitmentRepository.save(Fixture.createRecruitment(club));
        User normalUser = userRepository.save(Fixture.createUserWithRole(university,UserRole.NORMAL));
        String authorizationForBearer = authorizationForBearerAccessToken(normalUser);

        Recruitment newerRecruitment = recruitmentRepository.save(Fixture.createNewerRecruitment(club));

        // created_at이 초 단위 timestamp라 같은 초에 저장되면 값이 같아지므로 명시적으로 구분한다.
        ReflectionTestUtils.setField(recruitment, "createdAt", LocalDateTime.now().minusDays(1));
        ReflectionTestUtils.setField(newerRecruitment, "createdAt", LocalDateTime.now());
        recruitmentRepository.saveAll(List.of(recruitment, newerRecruitment));

        // when
        ExtractableResponse<Response> response = RestAssured.given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header("Authorization", authorizationForBearer)
                .when().get(prefixUrl + "/recruitments/club/recent/{clubId}", club.getId())
                .then().log().all()
                .extract();

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());

        RecentRecruitmentOfClubResponse recruitmentOfClubResponse = getDataFromResponse(response,
                RecentRecruitmentOfClubResponse.class);

        assertThat(recruitmentOfClubResponse.id()).isEqualTo(newerRecruitment.getId());
    }

    @Test
    @DisplayName("특정 모집글을 상세 조회한다.")
    void getSpecificRecruitment() {
        // given
        club = clubRepository.save(Fixture.createClub(university));
        recruitment = recruitmentRepository.save(Fixture.createRecruitment(club));
        User user = userRepository.save(Fixture.createUserWithRole(university,UserRole.NORMAL));
        String authorizationForBearer = authorizationForBearerAccessToken(user);

        // when
        ExtractableResponse<Response> response = RestAssured.given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header("Authorization", authorizationForBearer)
                .pathParam("recruitmentId", recruitment.getId())
                .when().get(prefixUrl + "/recruitments/{recruitmentId}")
                .then().log().all()
                .extract();

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());

        Long id = response.jsonPath().getLong("data.id");
        assertThat(id).isEqualTo(recruitment.getId());
    }
}
