package com.greedy.mokkoji.club.controller;

import com.greedy.mokkoji.api.club.dto.request.ClubUpdateRequest;
import com.greedy.mokkoji.api.club.dto.response.ClubDetailResponse;
import com.greedy.mokkoji.api.club.dto.response.ClubUpdateResponse;
import com.greedy.mokkoji.api.club.dto.response.allClubs.AllClubsResponse;
import com.greedy.mokkoji.api.club.dto.response.allClubs.ClubPreviewResponse;
import com.greedy.mokkoji.api.club.dto.response.allClubs.RecruitmentPreviewResponse;
import com.greedy.mokkoji.api.pagination.dto.PageResponse;
import com.greedy.mokkoji.common.ControllerTest;
import com.greedy.mokkoji.common.fixture.Fixture;
import com.greedy.mokkoji.common.response.APIErrorResponse;
import com.greedy.mokkoji.db.club.entity.Club;
import com.greedy.mokkoji.db.favorite.entity.Favorite;
import com.greedy.mokkoji.db.recruitment.entity.Recruitment;
import com.greedy.mokkoji.db.university.entity.University;
import com.greedy.mokkoji.db.user.entity.User;
import com.greedy.mokkoji.enums.club.ClubAffiliation;
import com.greedy.mokkoji.enums.club.ClubCategory;
import com.greedy.mokkoji.enums.message.FailMessage;
import com.greedy.mokkoji.enums.recruitment.RecruitStatus;
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

    private University university;
    private User user;
    private Club club;
    private Recruitment recruitment;
    private Favorite favorite;

    @BeforeEach
    @Transactional
    void setUp() {
        prepareData();
    }

    private void prepareData() {
        university = universityRepository.save(Fixture.createUniversity());
        user = userRepository.save(Fixture.createUser());
        club = clubRepository.save(Fixture.createClub(university));
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
        final ClubDetailResponse actual = getDataFromResponse(response, ClubDetailResponse.class);
        final ClubDetailResponse expected = ClubDetailResponse.of(
                club.getId(),
                club.getName(),
                club.getClubCategory(),
                club.getClubAffiliation(),
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

        final RecruitmentPreviewResponse recruitmentPreviewResponse =
                RecruitmentPreviewResponse.builder()
                        .id(recruitment.getId())
                        .recruitStart(recruitment.getRecruitStart())
                        .recruitEnd(recruitment.getRecruitEnd())
                        .recruitStatus(RecruitStatus.from(
                                recruitment.isAlwaysRecruiting(),
                                recruitment.getRecruitStart(),
                                recruitment.getRecruitEnd()
                        ))
                        .build();

        final List<ClubPreviewResponse> clubResponses =
                List.of(ClubPreviewResponse.builder()
                        .id(club.getId())
                        .name(club.getName())
                        .description(club.getDescription())
                        .logo(club.getLogo())
                        .isFavorite(true)
                        .recruitmentPreviewResponse(recruitmentPreviewResponse)
                        .build());

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
                .param("universityCode", university.getCode().name())
                .param("page", pageNumber)
                .param("size", pageSize)
                .when().get(prefixUrl + "/clubs")
                .then().log().all()
                .extract();

        //when
        final int statusCode = response.statusCode();
        final AllClubsResponse actual = getDataFromResponse(response, AllClubsResponse.class);
        final AllClubsResponse expected = AllClubsResponse.of(clubResponses, pageResponse);

        assertThat(statusCode).isEqualTo(HttpStatus.OK.value());
        assertThat(expected).usingRecursiveComparison().isEqualTo(actual);
    }

    @Test
    @DisplayName("동아리장은 자신이 관리 중인 동아리 정보를 수정할 수 있다.")
    void updateClub() {
        //given
        final User clubMasterUser = userRepository.save(Fixture.createUserWithRole(UserRole.CLUB_MASTER));
        final Club managedClub = Fixture.createClub(university, clubMasterUser);
        ReflectionTestUtils.setField(managedClub, "logo", "logo.jpg");
        clubRepository.save(managedClub);
        String authorizationForBearer = authorizationForBearerAccessToken(clubMasterUser);

        final ClubUpdateRequest request = new ClubUpdateRequest(
                "수정된 동아리",
                ClubCategory.CULTURAL_ART,
                ClubAffiliation.DEPARTMENT_CLUB,
                "동아리 수정 완료",
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
        assertThat(actual.id()).isEqualTo(managedClub.getId());
        assertThat(actual.updateLogo()).isEqualTo("presignedPutUrl");
        assertThat(actual.deleteLogo()).isEqualTo("presignedDeleteUrl");

        final Club updatedClub = clubRepository.findById(managedClub.getId()).orElseThrow();
        assertThat(updatedClub.getName()).isEqualTo(request.name());
        assertThat(updatedClub.getClubCategory()).isEqualTo(request.category());
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
    @DisplayName("동아리장이 아닌 사용자가 동아리를 수정하면 403을 반환한다")
    void updateClubForbidden() {
        //given
        final User normalUser = userRepository.save(Fixture.createAnotherUser());
        String authorizationForBearer = authorizationForBearerAccessToken(normalUser);

        final ClubUpdateRequest request = new ClubUpdateRequest(
                "수정된 동아리",
                ClubCategory.CULTURAL_ART,
                ClubAffiliation.DEPARTMENT_CLUB,
                "동아리 수정",
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
}
