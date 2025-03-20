package com.greedy.mokkoji.club.controller;

import com.greedy.mokkoji.api.club.dto.club.ClubDetailResponse;
import com.greedy.mokkoji.api.club.dto.club.ClubResponse;
import com.greedy.mokkoji.api.club.dto.club.ClubSearchResponse;
import com.greedy.mokkoji.api.club.dto.page.PageResponse;
import com.greedy.mokkoji.common.ControllerTest;
import com.greedy.mokkoji.common.fixture.Fixture;
import com.greedy.mokkoji.db.club.entity.Club;
import com.greedy.mokkoji.db.favorite.entity.Favorite;
import com.greedy.mokkoji.db.recruitment.entity.Recruitment;
import com.greedy.mokkoji.db.user.entity.User;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

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
        when(appDataS3Client.getPresignedUrl(any())).thenReturn(Fixture.FIXTURE_CLUB_LOGO);

        //when
        final ExtractableResponse<Response> response = given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header("Authorization", authorizationForBearer)
                .when().get(prefixUrl + "/clubs/{clubId}", 1L)
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
                club.getLogo(),
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

        when(appDataS3Client.getPresignedUrl(any())).thenReturn(Fixture.FIXTURE_CLUB_LOGO);

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
        final ClubSearchResponse actual = getDataFromResponse(response, ClubSearchResponse.class);
        final ClubSearchResponse expected = ClubSearchResponse.of(clubResponses, pageResponse);

        assertThat(statusCode).isEqualTo(HttpStatus.OK.value());
        assertThat(expected).usingRecursiveComparison().isEqualTo(actual);
    }
}
