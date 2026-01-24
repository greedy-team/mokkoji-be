package com.greedy.mokkoji.recruitment.controller;

import com.greedy.mokkoji.api.recruitment.dto.request.CreateRecruitmentRequest;
import com.greedy.mokkoji.api.recruitment.dto.request.UpdateRecruitmentRequest;
import com.greedy.mokkoji.api.recruitment.dto.response.allRecruitment.AllRecruitmentResponse;
import com.greedy.mokkoji.api.recruitment.dto.response.allRecruitment.RecruitmentPreviewResponse;
import com.greedy.mokkoji.api.recruitment.dto.response.allRecruitmentOfClub.RecruitmentOfClubResponse;
import com.greedy.mokkoji.api.recruitment.dto.response.recentRecruitment.RecentRecruitmentOfClubResponse;
import com.greedy.mokkoji.common.ControllerTest;
import com.greedy.mokkoji.common.fixture.Fixture;
import com.greedy.mokkoji.common.response.APIErrorResponse;
import com.greedy.mokkoji.db.club.entity.Club;
import com.greedy.mokkoji.db.recruitment.entity.Recruitment;
import com.greedy.mokkoji.db.user.entity.User;
import com.greedy.mokkoji.enums.club.ClubAffiliation;
import com.greedy.mokkoji.enums.club.ClubCategory;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Stream;

import static com.greedy.mokkoji.common.fixture.Fixture.FIXTURE_RECRUITMENT_IMAGE_NAME;
import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

public class RecruitmentControllerTest extends ControllerTest {

    private Club club;
    private Recruitment recruitment;

    static Stream<UserRole> allowedRoles() {
        return Stream.of(UserRole.CLUB_MASTER, UserRole.GREEDY_ADMIN, UserRole.CLUB_ADMIN);
    }

    static Stream<UserRole> forbiddenRoles() {
        return Stream.of(UserRole.NORMAL);
    }

    @BeforeEach
    void setUp() {
        recruitmentImageRepository.deleteAll();
        recruitmentRepository.deleteAll();
        favoriteRepository.deleteAll();
        clubRepository.deleteAll();
        userRepository.deleteAll();
    }

    @ParameterizedTest
    @MethodSource("allowedRoles")
    @DisplayName("권한을 가진 관리자는 모집글을 생성할 수 있다.")
    void createRecruitment_allowedRoles_success(UserRole role) {
        //given
        club = clubRepository.save(Fixture.createClub());
        recruitment = recruitmentRepository.save(Fixture.createRecruitment(club));
        User adminUser = userRepository.save(Fixture.createUserWithRole(role));
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
        club = clubRepository.save(Fixture.createClub());
        recruitment = recruitmentRepository.save(Fixture.createRecruitment(club));
        User normalUser = userRepository.save(Fixture.createUserWithRole(role));
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
                FailMessage.FORBIDDEN.getCode(),
                FailMessage.FORBIDDEN.getMessage()
        );

        assertThat(actualStatusCode).isEqualTo(HttpStatus.FORBIDDEN.value());
        assertThat(actualResponse).usingRecursiveComparison().isEqualTo(expectedResponse);
    }

    @Test
    @DisplayName("토큰 없이 모집글 생성 요청 시 401을 반환한다.")
    void createRecruitment_withoutToken_shouldReturn401() {
        // given
        club = clubRepository.save(Fixture.createClub());
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
        club = clubRepository.save(Fixture.createClub());
        recruitment = recruitmentRepository.save(Fixture.createRecruitment(club));
        User adminUser = userRepository.save(Fixture.createUserWithRole(role));
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
        club = clubRepository.save(Fixture.createClub());
        recruitment = recruitmentRepository.save(Fixture.createRecruitment(club));
        User normalUser = userRepository.save(Fixture.createUserWithRole(role));
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
                FailMessage.FORBIDDEN.getCode(),
                FailMessage.FORBIDDEN.getMessage()
        );

        assertThat(actualStatusCode).isEqualTo(HttpStatus.FORBIDDEN.value());
        assertThat(actualResponse).usingRecursiveComparison().isEqualTo(expectedResponse);
    }

    @ParameterizedTest
    @MethodSource("allowedRoles")
    @DisplayName("권한을 가진 관리자는 모집글을 삭제할 수 있다.")
    void deleteRecruitment_allowedRoles_success(UserRole role) {
        // given
        club = clubRepository.save(Fixture.createClub());
        recruitment = recruitmentRepository.save(Fixture.createRecruitment(club));
        User adminUser = userRepository.save(Fixture.createUserWithRole(role));
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
        club = clubRepository.save(Fixture.createClub());
        recruitment = recruitmentRepository.save(Fixture.createRecruitment(club));
        User normalUser = userRepository.save(Fixture.createUserWithRole(role));
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
                FailMessage.FORBIDDEN.getCode(),
                FailMessage.FORBIDDEN.getMessage()
        );

        assertThat(actualStatusCode).isEqualTo(HttpStatus.FORBIDDEN.value());
        assertThat(actualResponse).usingRecursiveComparison().isEqualTo(expectedResponse);
    }

    @Test
    @DisplayName("특정 동아리의 모든 모집글을 최신순으로 조회한다.")
    void getAllRecruitmentOfClub_shouldReturnNewestFirst() {
        // given
        club = clubRepository.save(Fixture.createClub());
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
    @DisplayName("동아리의 최신 모집글을 조회한다.")
    void getRecentRecruitmentOfClub() {
        // given
        club = clubRepository.save(Fixture.createClub());
        recruitment = recruitmentRepository.save(Fixture.createRecruitment(club));
        User normalUser = userRepository.save(Fixture.createUserWithRole(UserRole.NORMAL));
        String authorizationForBearer = authorizationForBearerAccessToken(normalUser);

        Recruitment newerRecruitment = recruitmentRepository.save(Fixture.createNewerRecruitment(club));

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
        club = clubRepository.save(Fixture.createClub());
        recruitment = recruitmentRepository.save(Fixture.createRecruitment(club));
        User user = userRepository.save(Fixture.createUserWithRole(UserRole.NORMAL));
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

    @Test
    @DisplayName("전체 모집글 조회한다. - 소속 및 카테고리 필터링 동작을 검증한다.")
    void getAllRecruitment_filtersByAffiliationAndCategory() {
        // given
        User normaluser = userRepository.save(Fixture.createUserWithRole(UserRole.NORMAL));
        String authorizationForBearer = authorizationForBearerAccessToken(normaluser);

        //필터링에 포함될 동아리 데이터
        Club club1 = clubRepository.save(Fixture.createClubWithCategoryAndAffiliation(ClubCategory.ACADEMIC_CULTURAL,
                ClubAffiliation.DEPARTMENT_CLUB));
        Club club2 = clubRepository.save(Fixture.createClubWithCategoryAndAffiliation(ClubCategory.ACADEMIC_CULTURAL,
                ClubAffiliation.DEPARTMENT_CLUB));
        recruitmentRepository.save(Fixture.createRecruitment(club1));
        recruitmentRepository.save(Fixture.createRecruitment(club2));

        // 필터링에서 걸러질 동아리 데이터
        Club otherClub1 = clubRepository.save(
                Fixture.createClubWithCategoryAndAffiliation(ClubCategory.CULTURAL_ART, ClubAffiliation.CENTRAL_CLUB));
        Club otherClub2 = clubRepository.save(
                Fixture.createClubWithCategoryAndAffiliation(ClubCategory.CULTURAL_ART, ClubAffiliation.CENTRAL_CLUB));
        recruitmentRepository.save(Fixture.createRecruitment(otherClub1));
        recruitmentRepository.save(Fixture.createRecruitment(otherClub2));

        ClubAffiliation filteringByAffiliation = ClubAffiliation.DEPARTMENT_CLUB;
        ClubCategory filteringByCategory = ClubCategory.ACADEMIC_CULTURAL;

        // when
        ExtractableResponse<Response> response = RestAssured.given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header("Authorization", authorizationForBearer)
                .queryParam("page", 1)
                .queryParam("size", 10)
                .queryParam("affiliation", ClubAffiliation.DEPARTMENT_CLUB.name())
                .queryParam("category", ClubCategory.ACADEMIC_CULTURAL.name())
                .when().get(prefixUrl + "/recruitments")
                .then().log().all()
                .extract();

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());

        AllRecruitmentResponse data = getDataFromResponse(response, AllRecruitmentResponse.class);

        assertThat(data.recruitments()).hasSize(2);

        assertThat(data.recruitments()).allSatisfy(recruitmentPreviewResponse -> {
            assertThat(recruitmentPreviewResponse.club()).isNotNull();
            assertThat(recruitmentPreviewResponse.club().clubAffiliation()).isEqualTo(filteringByAffiliation);
            assertThat(recruitmentPreviewResponse.club().clubCategory()).isEqualTo(filteringByCategory);
            assertThat(recruitmentPreviewResponse.id()).isNotNull();
            assertThat(recruitmentPreviewResponse.title()).isNotBlank();
        });
    }

    @Test
    @DisplayName("전체 모집글 조회한다. - 동아리 당 최신 모집글 1개만 반환되는 것을 검증한다.")
    void getAllRecruitment_returnsOnlyLatestRecruitmentPerClub() {
        // given
        User normalUser = userRepository.save(Fixture.createUserWithRole(UserRole.NORMAL));
        String authorizationForBearer = authorizationForBearerAccessToken(normalUser);

        Club targetClub = clubRepository.save(Fixture.createClub());

        Recruitment olderRecruitment = recruitmentRepository.save(Fixture.createOrderRecruitment(targetClub));
        Recruitment newerRecruitment = recruitmentRepository.save(Fixture.createNewerRecruitment(targetClub));

        // when
        ExtractableResponse<Response> response = RestAssured.given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header("Authorization", authorizationForBearer)
                .queryParam("page", 1)
                .queryParam("size", 10)
                .queryParam("affiliation", targetClub.getClubAffiliation().name())
                .queryParam("category", targetClub.getClubCategory().name())
                .when().get(prefixUrl + "/recruitments")
                .then().log().all()
                .extract();

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());

        AllRecruitmentResponse data = getDataFromResponse(response, AllRecruitmentResponse.class);
        assertThat(data.recruitments()).hasSize(1);

        RecruitmentPreviewResponse recruitmentPreviewResponse = data.recruitments().get(0);
        assertThat(recruitmentPreviewResponse.id()).isEqualTo(newerRecruitment.getId());

        assertThat(newerRecruitment.getClub().getId()).isEqualTo(targetClub.getId());
    }

    @Test
    @DisplayName("전체 모집글 조회 - 페이징이 정삭적으로 동작하는지 검증한다.")
    void getAllRecruitment_pagingWorks() {
        // given
        User normalUser = userRepository.save(Fixture.createUserWithRole(UserRole.NORMAL));
        String authorizationForBearer = authorizationForBearerAccessToken(normalUser);

        ClubAffiliation filteringByAffiliation = ClubAffiliation.DEPARTMENT_CLUB;
        ClubCategory filteringByCategory = ClubCategory.ACADEMIC_CULTURAL;

        for (int i = 0; i < 11; i++) {
            Club club = clubRepository.save(
                    Fixture.createClubWithCategoryAndAffiliation(filteringByCategory, filteringByAffiliation));
            recruitmentRepository.save(Fixture.createNewerRecruitment(club));
        }

        // when: page=1, size= 10 일 경우
        ExtractableResponse<Response> response1 = RestAssured.given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header("Authorization", authorizationForBearer)
                .queryParam("page", 1)
                .queryParam("size", 10)
                .queryParam("affiliation", filteringByAffiliation)
                .queryParam("category", filteringByCategory)
                .when().get(prefixUrl + "/recruitments")
                .then().log().all()
                .extract();
        AllRecruitmentResponse allRecruitmentResponse1 = getDataFromResponse(response1, AllRecruitmentResponse.class);

        // then
        assertThat(allRecruitmentResponse1.recruitments()).hasSize(10);
        assertThat(allRecruitmentResponse1.page().page()).isEqualTo(1);
        assertThat(allRecruitmentResponse1.page().size()).isEqualTo(10);
        assertThat(allRecruitmentResponse1.page().totalElements()).isEqualTo(11);
        assertThat(allRecruitmentResponse1.page().totalPages()).isEqualTo(2);

        // when: page=2, size= 10 일 경우
        ExtractableResponse<Response> response2 = RestAssured.given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header("Authorization", authorizationForBearer)
                .queryParam("page", 2)
                .queryParam("size", 10)
                .queryParam("affiliation", filteringByAffiliation)
                .queryParam("category", filteringByCategory)
                .when().get(prefixUrl + "/recruitments")
                .then().log().all()
                .extract();
        AllRecruitmentResponse allRecruitmentResponse2 = getDataFromResponse(response2, AllRecruitmentResponse.class);

        // then
        assertThat(allRecruitmentResponse2.recruitments()).hasSize(1);
        assertThat(allRecruitmentResponse2.page().page()).isEqualTo(2);
        assertThat(allRecruitmentResponse2.page().totalElements()).isEqualTo(11);
        assertThat(allRecruitmentResponse2.page().totalPages()).isEqualTo(2);
    }

    @Test
    @DisplayName("전체 모집글 조회 - 정렬 정책(즐겨 찾기, 마감 임박, 모집 중, 상시 모집, 모집 전, 모집 마감)대로 정렬되는지 검증한다.")
    void getAllRecruitment_sortedByPolicy() {
        // given
        User normalUser = userRepository.save(Fixture.createUserWithRole(UserRole.NORMAL));
        String token = authorizationForBearerAccessToken(normalUser);

        ClubAffiliation filteringByAffiliation = ClubAffiliation.DEPARTMENT_CLUB;
        ClubCategory filteringByCategory = ClubCategory.ACADEMIC_CULTURAL;

        Club favoriteClub = clubRepository.save(
                Fixture.createClubWithCategoryAndAffiliation(filteringByCategory, filteringByAffiliation));
        Club imminetClub = clubRepository.save(
                Fixture.createClubWithCategoryAndAffiliation(filteringByCategory, filteringByAffiliation));
        Club openClub = clubRepository.save(
                Fixture.createClubWithCategoryAndAffiliation(filteringByCategory, filteringByAffiliation));
        Club alwaysClub = clubRepository.save(
                Fixture.createClubWithCategoryAndAffiliation(filteringByCategory, filteringByAffiliation));
        Club beforeClub = clubRepository.save(
                Fixture.createClubWithCategoryAndAffiliation(filteringByCategory, filteringByAffiliation));
        Club closedClub = clubRepository.save(
                Fixture.createClubWithCategoryAndAffiliation(filteringByCategory, filteringByAffiliation));

        LocalDateTime now = LocalDateTime.now();

        // 즐겨찾기
        Recruitment favoriteRecruitment = recruitmentRepository.save(
                Fixture.createRecruitmentWithTimes(favoriteClub, now.minusDays(2), now.plusDays(10), false)
        );
        favoriteRepository.save(Fixture.createFavorite(favoriteClub, normalUser));

        //모집 마감 임박
        Recruitment imminetRecruitment = recruitmentRepository.save(
                Fixture.createRecruitmentWithTimes(imminetClub, now.minusDays(1), now.plusMinutes(30), false)
        );

        //모집 중
        Recruitment openRecruitment = recruitmentRepository.save(
                Fixture.createRecruitmentWithTimes(openClub, now.minusDays(1), now.plusDays(30), false)
        );

        //상시 모집
        Recruitment alwaysRecruitment = recruitmentRepository.save(
                Fixture.createRecruitmentWithTimes(alwaysClub, now.minusDays(100), now.plusDays(1000), true)
        );

        //모집 전
        Recruitment beforeRecruitment = recruitmentRepository.save(
                Fixture.createRecruitmentWithTimes(beforeClub, now.plusDays(3), now.plusDays(10), false)
        );

        //모집 마감
        Recruitment closedRecruitment = recruitmentRepository.save(
                Fixture.createRecruitmentWithTimes(closedClub, now.minusDays(10), now.minusDays(1), false)
        );

        // when
        ExtractableResponse<Response> response = RestAssured.given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header("Authorization", token)
                .queryParam("page", 1)
                .queryParam("size", 10)
                .queryParam("affiliation", filteringByAffiliation)
                .queryParam("category", filteringByCategory)
                .when().get(prefixUrl + "/recruitments")
                .then().log().all()
                .extract();

        AllRecruitmentResponse data = getDataFromResponse(response, AllRecruitmentResponse.class);

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
        assertThat(data.recruitments()).hasSize(6);

        List<Long> ids = data.recruitments().stream()
                .map(RecruitmentPreviewResponse::id)
                .toList();

        assertThat(ids).containsExactly(
                favoriteRecruitment.getId(),
                imminetRecruitment.getId(),
                openRecruitment.getId(),
                alwaysRecruitment.getId(),
                beforeRecruitment.getId(),
                closedRecruitment.getId()
        );
    }
}
