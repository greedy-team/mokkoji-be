package com.greedy.mokkoji.favorite.controller;

import static org.assertj.core.api.Assertions.assertThat;

import com.greedy.mokkoji.api.favorite.dto.response.RecruitClubsResponse;
import com.greedy.mokkoji.common.ControllerTest;
import com.greedy.mokkoji.common.fixture.Fixture;
import com.greedy.mokkoji.common.response.APIErrorResponse;
import com.greedy.mokkoji.db.club.entity.Club;
import com.greedy.mokkoji.db.user.entity.User;
import com.greedy.mokkoji.enums.message.FailMessage;
import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

@SuppressWarnings("NonAsciiCharacters")
public class FavoriteControllerTest extends ControllerTest {

    private User user;
    private Club favoriteClub;
    private Club notFavoriteClub;

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
        favoriteClub = clubRepository.save(Fixture.createClub());
        notFavoriteClub = clubRepository.save(Fixture.createClub());
        recruitmentRepository.saveAll(
            List.of(Fixture.createRecruitment(favoriteClub), Fixture.createRecruitment(notFavoriteClub))
        );
        favoriteRepository.save(Fixture.createFavorite(favoriteClub, user));
    }

    @Test
    @DisplayName("즐겨찾기를 추가한다.")
    void addFavorite() {
        // given & when
        ExtractableResponse<Response> response = RestAssured.given().log().all()
            .header("Authorization", authorizationForBearerAccessToken(user))
            .pathParam("clubId", notFavoriteClub.getId())
            .when().post(prefixUrl + "/favorites/{clubId}")
            .then().log().all()
            .extract();

        // then
        final int actualStatusCode = response.statusCode();

        assertThat(actualStatusCode).isEqualTo(HttpStatus.CREATED.value());
    }

    @Test
    @DisplayName("이미 즐겨찾기된 동아리에 즐겨찾기 요청을 보내면 409 Conflict 응답을 반환한다.")
    void addFavorite_whenAlreadyFavorited_shouldReturn409() {
        // given & when
        ExtractableResponse<Response> response = RestAssured.given().log().all()
            .header("Authorization", authorizationForBearerAccessToken(user))
            .pathParam("clubId", favoriteClub.getId())
            .when().post(prefixUrl + "/favorites/{clubId}")
            .then().log().all()
            .extract();

        // then
        final int actualStatusCode = response.statusCode();
        final APIErrorResponse actualResponse = response.as(APIErrorResponse.class);
        final APIErrorResponse expectedResponse = new APIErrorResponse(FailMessage.CONFLICT_FAVORITE.getCode(),
            FailMessage.CONFLICT_FAVORITE.getMessage());

        assertThat(actualStatusCode).isEqualTo(HttpStatus.CONFLICT.value());
        assertThat(actualResponse).usingRecursiveComparison().isEqualTo(expectedResponse);
    }

    @Test
    @DisplayName("토큰 없이 즐겨찾기 추가 요청 시 401 Unauthorized 응답을 반환한다.")
    void addFavorite_withoutToken_shouldReturn401() {
        // given & when
        ExtractableResponse<Response> response = RestAssured.given().log().all()
            .pathParam("clubId", notFavoriteClub.getId())
            .when().post(prefixUrl + "/favorites/{clubId}")
            .then().log().all()
            .extract();

        // then
        final int actualStatusCode = response.statusCode();
        final APIErrorResponse actualResponse = response.as(APIErrorResponse.class);
        final APIErrorResponse expectedResponse = new APIErrorResponse(FailMessage.UNAUTHORIZED_EMPTY_HEADER.getCode(),
            FailMessage.UNAUTHORIZED_EMPTY_HEADER.getMessage());

        assertThat(actualStatusCode).isEqualTo(HttpStatus.UNAUTHORIZED.value());
        assertThat(actualResponse).usingRecursiveComparison().isEqualTo(expectedResponse);
    }

    @Test
    @DisplayName("즐겨찾기를 삭제한다.")
    void deleteFavorite() {
        // given & when
        ExtractableResponse<Response> response = RestAssured.given().log().all()
            .header("Authorization", authorizationForBearerAccessToken(user))
            .pathParam("clubId", favoriteClub.getId())
            .when().delete(prefixUrl + "/favorites/{clubId}")
            .then().log().all()
            .extract();

        // then
        final int actualStatusCode = response.statusCode();

        assertThat(actualStatusCode).isEqualTo(HttpStatus.NO_CONTENT.value());
    }

    @Test
    @DisplayName("즐겨찾기 되지 않은 동아리를 삭제하면 404 Not Found 응답을 반환한다.")
    void deleteFavorite_whenNotFavorited_shouldReturn404() {
        // given & when
        ExtractableResponse<Response> response = RestAssured.given().log().all()
            .header("Authorization", authorizationForBearerAccessToken(user))
            .pathParam("clubId", notFavoriteClub.getId())
            .when().delete(prefixUrl + "/favorites/{clubId}")
            .then().log().all()
            .extract();

        // then
        final int actualStatusCode = response.statusCode();
        final APIErrorResponse actualResponse = response.as(APIErrorResponse.class);
        final APIErrorResponse expectedResponse = new APIErrorResponse(FailMessage.NOT_FOUND_FAVORITE.getCode(),
            FailMessage.NOT_FOUND_FAVORITE.getMessage());

        assertThat(actualStatusCode).isEqualTo(HttpStatus.NOT_FOUND.value());
        assertThat(actualResponse).usingRecursiveComparison().isEqualTo(expectedResponse);
    }

    @Test
    @DisplayName("토큰 없이 즐겨찾기 삭제 요청 시 401 Unauthorized 응답을 반환한다.")
    void deleteFavorite_withoutToken_shouldReturn401() {
        // given & when
        ExtractableResponse<Response> response = RestAssured.given().log().all()
            .pathParam("clubId", notFavoriteClub.getId())
            .when().delete(prefixUrl + "/favorites/{clubId}")
            .then().log().all()
            .extract();

        // then
        final int actualStatusCode = response.statusCode();
        final APIErrorResponse actualResponse = response.as(APIErrorResponse.class);
        final APIErrorResponse expectedResponse = new APIErrorResponse(FailMessage.UNAUTHORIZED_EMPTY_HEADER.getCode(),
            FailMessage.UNAUTHORIZED_EMPTY_HEADER.getMessage());

        assertThat(actualStatusCode).isEqualTo(HttpStatus.UNAUTHORIZED.value());
        assertThat(actualResponse).usingRecursiveComparison().isEqualTo(expectedResponse);
    }

    @Test
    @DisplayName("즐겨찾기한 동아리를 조회한다.")
    void getFavoriteClubs() {
        // given & when
        ExtractableResponse<Response> response = RestAssured.given().log().all()
            .header("Authorization", authorizationForBearerAccessToken(user))
            .param("page", 1)
            .param("size", 5)
            .when().get(prefixUrl + "/favorites")
            .then().log().all()
            .extract();

        // then
        final int actualStatusCode = response.statusCode();

        assertThat(actualStatusCode).isEqualTo(HttpStatus.OK.value());
    }

    @Test
    @DisplayName("즐겨찾기 되어 있는 동아리 중, 해당 연월에 모집하는 동아리의 모집 정보를 조회한다.")
    void getRecruitClubs() {
        ExtractableResponse<Response> response = RestAssured.given().log().all()
            .header("Authorization", authorizationForBearerAccessToken(user))
            .param("yearMonth", "2025-01")
            .when().get(prefixUrl + "/favorites/recruit")
            .then().log().all()
            .extract();

        final int actualStatusCode = response.statusCode();

        assertThat(actualStatusCode).isEqualTo(HttpStatus.OK.value());

        List<RecruitClubsResponse> recruitList =
            response.jsonPath().getList("data", RecruitClubsResponse.class);

        RecruitClubsResponse recruitClubResponse = recruitList.get(0);

        assertThat(recruitClubResponse.clubId()).isEqualTo(favoriteClub.getId());
        assertThat(recruitClubResponse.clubName()).isEqualTo("그리디");
        assertThat(recruitClubResponse.recruitStart()).isEqualTo(LocalDateTime.of(2025, 1, 1, 12, 0, 0));
        assertThat(recruitClubResponse.recruitEnd()).isEqualTo(LocalDateTime.of(2025, 2, 2, 12, 0, 0));
    }

    @DisplayName("특정 연월에 모집 중인 즐겨찾기 동아리의 최신 모집 정보를 조회한다. - 모집 시작일이 겹치는 경우를 검증한다.")
    @Test
    void getRecruitClubsWhenRecruitStartInYearMonth() {
        recruitmentRepository.saveAll(
            List.of(Fixture.createRecruitmentOfAugust(favoriteClub),
                Fixture.createLastedRecruitmentOfAugust(favoriteClub))
        );

        ExtractableResponse<Response> response = RestAssured.given().log().all()
            .header("Authorization", authorizationForBearerAccessToken(user))
            .param("yearMonth", "2025-08")
            .when().get(prefixUrl + "/favorites/recruit")
            .then().log().all()
            .extract();

        final int actualStatusCode = response.statusCode();

        assertThat(actualStatusCode).isEqualTo(HttpStatus.OK.value());

        List<RecruitClubsResponse> recruitList =
            response.jsonPath().getList("data", RecruitClubsResponse.class);

        RecruitClubsResponse recruitClubResponse = recruitList.get(0);

        assertThat(recruitClubResponse.clubId()).isEqualTo(favoriteClub.getId());
        assertThat(recruitClubResponse.clubName()).isEqualTo("그리디");
        assertThat(recruitClubResponse.recruitStart()).isEqualTo(LocalDateTime.of(2025, 8, 1, 12, 0, 0));
        assertThat(recruitClubResponse.recruitEnd()).isEqualTo(LocalDateTime.of(2025, 9, 2, 12, 0, 0));
    }

    @DisplayName("특정 연월에 모집 중인 즐겨찾기 동아리의 최신 모집 정보를 조회한다. - 모집 마감일이 겹치는 경우를 검증한다.")
    @Test
    void getRecruitClubsWhenRecruitEndInYearMonth() {
        recruitmentRepository.saveAll(
            List.of(Fixture.createRecruitmentOfAugust(favoriteClub),
                Fixture.createLastedRecruitmentOfAugust(favoriteClub))
        );

        ExtractableResponse<Response> response = RestAssured.given().log().all()
            .header("Authorization", authorizationForBearerAccessToken(user))
            .param("yearMonth", "2025-09")
            .when().get(prefixUrl + "/favorites/recruit")
            .then().log().all()
            .extract();

        final int actualStatusCode = response.statusCode();

        assertThat(actualStatusCode).isEqualTo(HttpStatus.OK.value());

        List<RecruitClubsResponse> recruitList =
            response.jsonPath().getList("data", RecruitClubsResponse.class);

        RecruitClubsResponse recruitClubResponse = recruitList.get(0);

        assertThat(recruitClubResponse.clubId()).isEqualTo(favoriteClub.getId());
        assertThat(recruitClubResponse.clubName()).isEqualTo("그리디");
        assertThat(recruitClubResponse.recruitStart()).isEqualTo(LocalDateTime.of(2025, 8, 1, 12, 0, 0));
        assertThat(recruitClubResponse.recruitEnd()).isEqualTo(LocalDateTime.of(2025, 9, 2, 12, 0, 0));
    }

    @DisplayName("특정 연월에 해당하는 즐겨찾기 동아리의 모집글이 없으면 빈 리스트를 반환한다.")
    @Test
    void getRecruitClubsWhenNoRecruitmentInYearMonth() {
        recruitmentRepository.saveAll(
            List.of(Fixture.createRecruitmentOfAugust(favoriteClub),
                Fixture.createLastedRecruitmentOfAugust(favoriteClub))
        );

        ExtractableResponse<Response> response = RestAssured.given().log().all()
            .header("Authorization", authorizationForBearerAccessToken(user))
            .param("yearMonth", "2025-10")
            .when().get(prefixUrl + "/favorites/recruit")
            .then().log().all()
            .extract();

        final int actualStatusCode = response.statusCode();

        assertThat(actualStatusCode).isEqualTo(HttpStatus.OK.value());

        List<RecruitClubsResponse> recruitList =
            response.jsonPath().getList("data", RecruitClubsResponse.class);

        assertThat(recruitList).isEmpty();
    }
}
