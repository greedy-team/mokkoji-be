package com.greedy.mokkoji.recruitment.controller;

import com.greedy.mokkoji.common.ControllerTest;
import com.greedy.mokkoji.common.fixture.Fixture;
import com.greedy.mokkoji.common.response.APIErrorResponse;
import com.greedy.mokkoji.db.club.entity.Club;
import com.greedy.mokkoji.db.recruitment.entity.Recruitment;
import com.greedy.mokkoji.db.recruitment.entity.RecruitmentApplyClick;
import com.greedy.mokkoji.db.university.entity.University;
import com.greedy.mokkoji.enums.message.FailMessage;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import java.util.List;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

public class RecruitmentApplyClickControllerTest extends ControllerTest {

    private University university;
    private Club club;
    private Recruitment recruitment;

    @BeforeEach
    void setUp() {
        university = universityRepository.save(Fixture.createUniversity());
        club = clubRepository.save(Fixture.createClub(university));
        recruitment = recruitmentRepository.save(Fixture.createRecruitment(club));
    }

    @Test
    @DisplayName("인증 없이 호출해도 모집글과 클릭 시각으로 기록된다.")
    void recordApplyClick_withoutAuthentication_savesClick() {
        //when
        final ExtractableResponse<Response> response = requestApplyClick(recruitment.getId(), null);

        //then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());

        final List<RecruitmentApplyClick> clicks = recruitmentApplyClickRepository.findAll();
        assertThat(clicks).hasSize(1);
        assertThat(clicks.get(0).getRecruitment().getId()).isEqualTo(recruitment.getId());
        assertThat(clicks.get(0).getClickedAt()).isNotNull();
    }

    @Test
    @DisplayName("쓸 수 없는 토큰을 보내도 401이 아니라 클릭이 기록된다.")
    void recordApplyClick_withUnusableToken_savesClick() {
        //when
        final ExtractableResponse<Response> response = requestApplyClick(recruitment.getId(), "Bearer 쓸수없는토큰");

        //then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
        assertThat(recruitmentApplyClickRepository.findAll()).hasSize(1);
    }

    @Test
    @DisplayName("존재하지 않는 모집글에 대한 클릭은 404를 반환하고 기록되지 않는다.")
    void recordApplyClick_notFoundRecruitment_returnsNotFound() {
        //given
        final Long notExistRecruitmentId = recruitment.getId() + 1000L;

        //when
        final ExtractableResponse<Response> response = requestApplyClick(notExistRecruitmentId, null);

        //then
        final APIErrorResponse error = response.as(APIErrorResponse.class);
        assertThat(response.statusCode()).isEqualTo(FailMessage.NOT_FOUNT_RECRUITMENT.getHttpStatus().value());
        assertThat(error.code()).isEqualTo(FailMessage.NOT_FOUNT_RECRUITMENT.getCode());
        assertThat(recruitmentApplyClickRepository.findAll()).isEmpty();
    }

    @Test
    @DisplayName("여러 번 누르면 누른 횟수만큼 모두 기록된다.")
    void recordApplyClick_multipleClicks_savesEveryClick() {
        //when
        requestApplyClick(recruitment.getId(), null);
        requestApplyClick(recruitment.getId(), null);
        requestApplyClick(recruitment.getId(), null);

        //then
        assertThat(recruitmentApplyClickRepository.findAll()).hasSize(3);
    }

    private ExtractableResponse<Response> requestApplyClick(final Long recruitmentId, final String authorization) {
        var request = given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE);

        if (authorization != null) {
            request = request.header("Authorization", authorization);
        }

        return request
                .when().post(prefixUrl + "/recruitments/{recruitmentId}/apply-click", recruitmentId)
                .then().log().all()
                .extract();
    }
}
