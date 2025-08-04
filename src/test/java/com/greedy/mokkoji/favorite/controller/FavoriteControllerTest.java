package com.greedy.mokkoji.favorite.controller;

import com.greedy.mokkoji.common.ControllerTest;
import com.greedy.mokkoji.common.fixture.Fixture;
import com.greedy.mokkoji.common.response.APIErrorResponse;
import com.greedy.mokkoji.db.club.entity.Club;
import com.greedy.mokkoji.db.user.entity.User;
import com.greedy.mokkoji.enums.message.FailMessage;
import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

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
        recruitmentRepository.saveAll(List.of(Fixture.createRecruitment(favoriteClub), Fixture.createRecruitment(notFavoriteClub)));
        favoriteRepository.save(Fixture.createFavorite(favoriteClub, user));
    }

    @Test
    void 즐겨찾기_성공_테스트() {
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
    void 즐겨찾기가_되어있는_곳에_즐겨찾기_요청시_409를_응답한다() {
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
        final APIErrorResponse expectedResponse = new APIErrorResponse(FailMessage.CONFLICT_FAVORITE.getCode(), FailMessage.CONFLICT_FAVORITE.getMessage());

        assertThat(actualStatusCode).isEqualTo(HttpStatus.CONFLICT.value());
        assertThat(actualResponse).usingRecursiveComparison().isEqualTo(expectedResponse);
    }


    @Test
    void 즐겨찾기_삭제_성공_테스트() {
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
    void 즐겨찾기_안_되어_있는곳에_즐겨찾기_삭제시_401을_응답한다() {
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
        final APIErrorResponse expectedResponse = new APIErrorResponse(FailMessage.NOT_FOUND_FAVORITE.getCode(), FailMessage.NOT_FOUND_FAVORITE.getMessage());

        assertThat(actualStatusCode).isEqualTo(HttpStatus.NOT_FOUND.value());
        assertThat(actualResponse).usingRecursiveComparison().isEqualTo(expectedResponse);
    }

    @Test
    void 토큰없이_즐겨찾기_추가시_404에러를_응답한다() {
        // given & when
        ExtractableResponse<Response> response = RestAssured.given().log().all()
                .pathParam("clubId", notFavoriteClub.getId())
                .when().post(prefixUrl + "/favorites/{clubId}")
                .then().log().all()
                .extract();

        // then
        final int actualStatusCode = response.statusCode();
        final APIErrorResponse actualResponse = response.as(APIErrorResponse.class);
        final APIErrorResponse expectedResponse = new APIErrorResponse(FailMessage.UNAUTHORIZED_EMPTY_HEADER.getCode(), FailMessage.UNAUTHORIZED_EMPTY_HEADER.getMessage());

        assertThat(actualStatusCode).isEqualTo(HttpStatus.UNAUTHORIZED.value());
        assertThat(actualResponse).usingRecursiveComparison().isEqualTo(expectedResponse);
    }

    @Test
    void 토큰없이_즐겨찾기_삭제시_404에러를_응답한다() {
        // given & when
        ExtractableResponse<Response> response = RestAssured.given().log().all()
                .pathParam("clubId", notFavoriteClub.getId())
                .when().delete(prefixUrl + "/favorites/{clubId}")
                .then().log().all()
                .extract();

        // then
        final int actualStatusCode = response.statusCode();
        final APIErrorResponse actualResponse = response.as(APIErrorResponse.class);
        final APIErrorResponse expectedResponse = new APIErrorResponse(FailMessage.UNAUTHORIZED_EMPTY_HEADER.getCode(), FailMessage.UNAUTHORIZED_EMPTY_HEADER.getMessage());

        assertThat(actualStatusCode).isEqualTo(HttpStatus.UNAUTHORIZED.value());
        assertThat(actualResponse).usingRecursiveComparison().isEqualTo(expectedResponse);
    }
}
