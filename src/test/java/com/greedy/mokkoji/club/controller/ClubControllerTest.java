package com.greedy.mokkoji.club.controller;

import com.greedy.mokkoji.api.club.dto.request.ClubCreateRequest;
import com.greedy.mokkoji.api.club.dto.request.ClubUpdateRequest;
import com.greedy.mokkoji.api.club.dto.response.ClubDetailResponse;
import com.greedy.mokkoji.api.club.dto.response.ClubManageDetailResponse;
import com.greedy.mokkoji.api.club.dto.response.ClubResponse;
import com.greedy.mokkoji.api.club.dto.response.ClubUpdateResponse;
import com.greedy.mokkoji.api.club.dto.response.ClubsPaginationResponse;
import com.greedy.mokkoji.api.pagination.dto.PageResponse;
import com.greedy.mokkoji.common.ControllerTest;
import com.greedy.mokkoji.common.fixture.Fixture;
import com.greedy.mokkoji.common.response.APIErrorResponse;
import com.greedy.mokkoji.db.club.entity.Club;
import com.greedy.mokkoji.db.favorite.entity.Favorite;
import com.greedy.mokkoji.db.recruitment.entity.Recruitment;
import com.greedy.mokkoji.db.user.entity.User;
import com.greedy.mokkoji.enums.club.ClubAffiliation;
import com.greedy.mokkoji.enums.club.ClubCategory;
import com.greedy.mokkoji.enums.message.FailMessage;
import com.greedy.mokkoji.enums.user.UserRole;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class ClubControllerTest extends ControllerTest {

    private User user;
    private Club club;
    private Recruitment recruitment;
    private Favorite favorite;

    @BeforeEach
    @Transactional
    void setUp() {
        favoriteRepository.deleteAll();
        userRepository.deleteAll();
        recruitmentRepository.deleteAll();
        clubRepository.deleteAll();
        prepareData();
    }

    private void prepareData() {
        user = userRepository.save(Fixture.createUser());
        club = clubRepository.save(Fixture.createClub());
        favorite = favoriteRepository.save(Fixture.createFavorite(club, user));
        recruitment = recruitmentRepository.save(Fixture.createRecruitment(club));
    }

    @Test
    @DisplayName("동아리 상세 정보 조회를 할 수 있다.")
    void getClub() {
        //given
        String authorizationForBearer = authorizationForBearerAccessToken(user);
        when(appDataS3Client.getPublicUrl(any())).thenReturn(Fixture.FIXTURE_CLUB_LOGO);

        //when
        final ExtractableResponse<Response> response = given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header("Authorization", authorizationForBearer)
                .when().get(prefixUrl + "/clubs/{clubId}", club.getId())
                .then().log().all()
                .extract();

        //then
        final int statusCode = response.statusCode();
        final ClubDetailResponse actual = getDataFromResponse(response, ClubDetailResponse.class); //그리디 로고
        final ClubDetailResponse expected = ClubDetailResponse.of(
                club.getId(),
                club.getName(),
                club.getClubCategory().getDescription(),
                club.getClubAffiliation().getDescription(),
                club.getDescription(),
                recruitment.getRecruitStart(),
                recruitment.getRecruitEnd(),
                Fixture.FIXTURE_CLUB_LOGO,
                true,
                club.getInstagram(),
                recruitment.getContent()
        );

        assertThat(statusCode).isEqualTo(HttpStatus.OK.value());
        assertThat(actual).usingRecursiveComparison().isEqualTo(expected);
    }

    @Test
    @DisplayName("동아리 전체 정보를 조회할 수 있다.")
    void getClubs() {
        //given
        String authorizationForBearer = authorizationForBearerAccessToken(user);

        final List<ClubResponse> clubResponses = List.of(ClubResponse.of(
                club.getId(),
                club.getName(),
                club.getClubCategory().getDescription(),
                club.getClubAffiliation().getDescription(),
                club.getDescription(),
                recruitment.getRecruitStart(),
                recruitment.getRecruitEnd(),
                club.getLogo(),
                true
        ));

        final int pageNumber = 1;
        final int pageSize = 10;
        final PageResponse pageResponse = PageResponse.of(
                pageNumber,
                pageSize,
                1,
                1
        );

        when(appDataS3Client.getPublicUrl(any())).thenReturn(Fixture.FIXTURE_CLUB_LOGO);

        final ExtractableResponse<Response> response = given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header("Authorization", authorizationForBearer)
                .param("page", pageNumber)
                .param("size", pageSize)
                .when().get(prefixUrl + "/clubs")
                .then().log().all()
                .extract();

        //when
        final int statusCode = response.statusCode();
        final ClubsPaginationResponse actual = getDataFromResponse(response, ClubsPaginationResponse.class);
        final ClubsPaginationResponse expected = ClubsPaginationResponse.of(clubResponses, pageResponse);

        assertThat(statusCode).isEqualTo(HttpStatus.OK.value());
        assertThat(expected).usingRecursiveComparison().isEqualTo(actual);
    }

    @Test
    @DisplayName("동아리를 생성할 수 있다.")
    void createClub() {
        //given
        final User adminUser = User.builder()
                .name("관리자")
                .email("admin@test.com")
                .studentId("12345678")
                .grade("4")
                .department("컴퓨터공학과")
                .role(UserRole.CLUB_ADMIN)
                .build();
        userRepository.save(adminUser);
        String authorizationForBearer = authorizationForBearerAccessToken(adminUser);

        final ClubCreateRequest request = new ClubCreateRequest(
                "새로운 동아리",
                ClubCategory.ACADEMIC_CULTURAL,
                ClubAffiliation.DEPARTMENT_CLUB,
                adminUser.getStudentId()
        );

        //when
        final ExtractableResponse<Response> response = given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header("Authorization", authorizationForBearer)
                .body(request)
                .when().post(prefixUrl + "/clubs")
                .then().log().all()
                .extract();

        //then
        final int statusCode = response.statusCode();
        Club createdClub = clubRepository.findByClubMasterStudentId(adminUser.getStudentId()).get(0);
        assertThat(statusCode).isEqualTo(HttpStatus.CREATED.value());
        assertThat(createdClub.getName()).isEqualTo(request.name());
        assertThat(createdClub.getClubAffiliation()).isEqualTo(request.affiliation());
        assertThat(createdClub.getClubCategory()).isEqualTo(request.category());
        assertThat(createdClub.getClubMasterStudentId()).isEqualTo(request.clubMasterStudentId());
    }

    @Test
    @DisplayName("동아리 관리 상세 정보 조회를 할 수 있다.")
    void getClubManageDetail() {
        //given
        final User adminUser = User.builder()
                .name("관리자")
                .email("admin@test.com")
                .studentId("12345678")
                .grade("4")
                .department("컴퓨터공학과")
                .role(UserRole.CLUB_ADMIN)
                .build();
        userRepository.save(adminUser);

        final Club managedClub = Club.builder()
                .name("관리할 동아리")
                .clubAffiliation(ClubAffiliation.CENTRAL_CLUB)
                .clubCategory(ClubCategory.SPORTS)
                .clubMasterStudentId(adminUser.getStudentId())
                .description("동아리 설명")
                .instagram("instagram.com")
                .build();
        clubRepository.save(managedClub);
        String authorizationForBearer = authorizationForBearerAccessToken(adminUser);
        when(appDataS3Client.getPresignedUrl(null)).thenReturn(null);

        //when
        final ExtractableResponse<Response> response = given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header("Authorization", authorizationForBearer)
                .when().get(prefixUrl + "/clubs/manage/{clubId}", managedClub.getId())
                .then().log().all()
                .extract();

        //then
        final int statusCode = response.statusCode();
        final ClubManageDetailResponse actual = getDataFromResponse(response, ClubManageDetailResponse.class);
        final ClubManageDetailResponse expected = ClubManageDetailResponse.of(
                managedClub.getName(),
                managedClub.getClubCategory().name(),
                managedClub.getClubAffiliation().name(),
                managedClub.getDescription(),
                managedClub.getLogo(),
                managedClub.getInstagram()
        );

        assertThat(statusCode).isEqualTo(HttpStatus.OK.value());
        assertThat(actual).usingRecursiveComparison().isEqualTo(expected);
    }

    @Test
    @DisplayName("동아리 정보를 수정할 수 있다.")
    void updateClub() {
        //given
        final User clubMasterUser = User.builder()
                .name("동아리장")
                .email("master@test.com")
                .grade("3")
                .studentId("21123456")
                .role(UserRole.CLUB_MASTER)
                .build();
        userRepository.save(clubMasterUser);
        String authorizationForBearer = authorizationForBearerAccessToken(clubMasterUser);

        final Club managedClub = Club.builder()
                .name("수정할 동아리")
                .clubCategory(ClubCategory.SPORTS)
                .clubAffiliation(ClubAffiliation.CENTRAL_CLUB)
                .clubMasterStudentId(clubMasterUser.getStudentId())
                .build();
        ReflectionTestUtils.setField(managedClub, "logo", "logo.jpg");
        clubRepository.save(managedClub);

        final ClubUpdateRequest request = new ClubUpdateRequest(
                "수정된 동아리",
                ClubCategory.CULTURAL_ART,
                ClubAffiliation.DEPARTMENT_CLUB,
                "동아리 수정 완료",
                "12345678",
                "new-logo.jpg",
                "new-instagram.com"
        );

        when(appDataS3Client.getPresignedPutUrl(any())).thenReturn("presignedPutUrl");
        when(appDataS3Client.getPresignedDeleteUrl(any())).thenReturn("presignedDeleteUrl");

        //when
        final ExtractableResponse<Response> response = given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header("Authorization", authorizationForBearer)
                .body(request)
                .when().patch(prefixUrl + "/clubs/manage/{clubId}", managedClub.getId())
                .then().log().all()
                .extract();

        //then
        final int statusCode = response.statusCode();
        ClubUpdateResponse actual = getDataFromResponse(response, ClubUpdateResponse.class);

        assertThat(statusCode).isEqualTo(HttpStatus.OK.value());
        assertThat(actual.updateLogo()).isEqualTo("presignedPutUrl");
        assertThat(actual.deleteLogo()).isEqualTo("presignedDeleteUrl");

        final Club updatedClub = clubRepository.findById(managedClub.getId()).orElseThrow();
        assertThat(updatedClub.getName()).isEqualTo(request.name());
        assertThat(updatedClub.getClubCategory()).isEqualTo(request.category());
        assertThat(updatedClub.getClubMasterStudentId()).isEqualTo(request.clubMasterStudentId());
        assertThat(updatedClub.getClubAffiliation()).isEqualTo(request.affiliation());
        assertThat(updatedClub.getLogo()).contains("new-logo_");
        assertThat(updatedClub.getInstagram()).isEqualTo(request.instagram());
    }

    @Test
    @DisplayName("존재하지 않는 동아리를 조회하면 404를 반환한다")
    void getClubNotFound() {
        //given
        Long nonExistentClubId = 99999L;
        String authorizationForBearer = authorizationForBearerAccessToken(user);

        //when
        final ExtractableResponse<Response> response = given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header("Authorization", authorizationForBearer)
                .when().get(prefixUrl + "/clubs/{clubId}", nonExistentClubId)
                .then().log().all()
                .extract();

        //then
        final int actualStatusCode = response.statusCode();
        final APIErrorResponse actualResponse = response.as(APIErrorResponse.class);
        final APIErrorResponse expectedResponse = new APIErrorResponse(
                FailMessage.NOT_FOUND_CLUB.getCode(),
                FailMessage.NOT_FOUND_CLUB.getMessage()
        );

        assertThat(actualStatusCode).isEqualTo(HttpStatus.NOT_FOUND.value());
        assertThat(actualResponse).usingRecursiveComparison().isEqualTo(expectedResponse);
    }

    @Test
    @DisplayName("권한 없는 사용자가 동아리를 생성하면 403을 반환한다")
    void createClubForbidden() {
        //given
        final User normalUser = User.builder()
                .name("일반사용자")
                .email("normal@test.com")
                .studentId("11112222")
                .grade("2")
                .department("소프트웨어학과")
                .role(UserRole.NORMAL)
                .build();
        userRepository.save(normalUser);
        String authorizationForBearer = authorizationForBearerAccessToken(normalUser);

        final ClubCreateRequest request = new ClubCreateRequest(
                "새로운 동아리",
                ClubCategory.ACADEMIC_CULTURAL,
                ClubAffiliation.DEPARTMENT_CLUB,
                null
        );

        //when
        final ExtractableResponse<Response> response = given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header("Authorization", authorizationForBearer)
                .body(request)
                .when().post(prefixUrl + "/clubs")
                .then().log().all()
                .extract();

        //then
        final int actualStatusCode = response.statusCode();
        final APIErrorResponse actualResponse = response.as(APIErrorResponse.class);
        final APIErrorResponse expectedResponse = new APIErrorResponse(
                FailMessage.FORBIDDEN_REGISTER_CLUB.getCode(),
                FailMessage.FORBIDDEN_REGISTER_CLUB.getMessage()
        );

        assertThat(actualStatusCode).isEqualTo(HttpStatus.FORBIDDEN.value());
        assertThat(actualResponse).usingRecursiveComparison().isEqualTo(expectedResponse);
    }

    @Test
    @DisplayName("권한 없는 사용자가 동아리를 수정하면 403을 반환한다")
    void updateClubForbidden() {
        //given
        final User normalUser = User.builder()
                .name("일반사용자")
                .email("normal2@test.com")
                .studentId("22223333")
                .grade("2")
                .department("소프트웨어학과")
                .role(UserRole.NORMAL)
                .build();
        userRepository.save(normalUser);
        String authorizationForBearer = authorizationForBearerAccessToken(normalUser);

        final ClubUpdateRequest request = new ClubUpdateRequest(
                "수정된 동아리",
                ClubCategory.CULTURAL_ART,
                ClubAffiliation.DEPARTMENT_CLUB,
                "동아리 수정",
                null,
                null,
                null
        );

        //when
        final ExtractableResponse<Response> response = given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header("Authorization", authorizationForBearer)
                .body(request)
                .when().patch(prefixUrl + "/clubs/manage/{clubId}", club.getId())
                .then().log().all()
                .extract();

        //then
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
    @DisplayName("존재하지 않는 동아리장 학번으로 동아리를 생성하면 404를 반환한다")
    void createClubWithNonExistentClubMaster() {
        //given
        final String nonExistentStudentId = "99999999";

        final User adminUser = User.builder()
                .name("관리자")
                .email("admin2@test.com")
                .studentId("99998888")
                .grade("4")
                .department("컴퓨터공학과")
                .role(UserRole.CLUB_ADMIN)
                .build();
        userRepository.save(adminUser);
        String authorizationForBearer = authorizationForBearerAccessToken(adminUser);

        final ClubCreateRequest request = new ClubCreateRequest(
                "새로운 동아리",
                ClubCategory.ACADEMIC_CULTURAL,
                ClubAffiliation.DEPARTMENT_CLUB,
                nonExistentStudentId
        );

        //when
        final ExtractableResponse<Response> response = given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header("Authorization", authorizationForBearer)
                .body(request)
                .when().post(prefixUrl + "/clubs")
                .then().log().all()
                .extract();

        //then
        final int actualStatusCode = response.statusCode();
        final APIErrorResponse actualResponse = response.as(APIErrorResponse.class);
        final APIErrorResponse expectedResponse = new APIErrorResponse(
                FailMessage.NOT_FOUND_USER.getCode(),
                FailMessage.NOT_FOUND_USER.getMessage()
        );

        assertThat(actualStatusCode).isEqualTo(HttpStatus.NOT_FOUND.value());
        assertThat(actualResponse).usingRecursiveComparison().isEqualTo(expectedResponse);
    }
}
