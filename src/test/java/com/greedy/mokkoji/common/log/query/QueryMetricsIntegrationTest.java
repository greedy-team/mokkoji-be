package com.greedy.mokkoji.common.log.query;

import com.greedy.mokkoji.common.ControllerTest;
import com.greedy.mokkoji.common.fixture.Fixture;
import com.greedy.mokkoji.db.club.entity.Club;
import com.greedy.mokkoji.db.university.entity.University;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("NonAsciiCharacters")
@DisplayName("쿼리 메트릭 통합 테스트")
public class QueryMetricsIntegrationTest extends ControllerTest {

    @Autowired
    private MeterRegistry meterRegistry;

    private University university;

    @BeforeEach
    void setUp() {
        prepareData();
    }

    private void prepareData() {
        university = universityRepository.save(Fixture.createUniversity());
        final Club club = clubRepository.save(Fixture.createClub(university));
        recruitmentRepository.save(Fixture.createRecruitment(club));
    }

    @Test
    @DisplayName("요청이 끝나면 해당 API의 쿼리 수·쿼리 시간 메트릭이 기록된다.")
    void recordQueryMetricsAfterRequest() {
        //given
        final String uri = prefixUrl + "/clubs/search";

        //when
        RestAssured.given().log().ifValidationFails()
                .queryParam("universityCode", university.getCode())
                .queryParam("page", 1)
                .queryParam("size", 10)
                .when().get(uri)
                .then().statusCode(200);

        //then
        final DistributionSummary queryCount = meterRegistry.find("request.db.query.count")
                .tag("method", "GET")
                .tag("uri", uri)
                .summary();
        assertThat(queryCount).isNotNull();
        assertThat(queryCount.count()).isEqualTo(1);
        assertThat(queryCount.totalAmount()).isGreaterThan(0);

        final Timer queryTime = meterRegistry.find("request.db.query.time")
                .tag("method", "GET")
                .tag("uri", uri)
                .timer();
        assertThat(queryTime).isNotNull();
        assertThat(queryTime.totalTime(TimeUnit.NANOSECONDS)).isGreaterThan(0);
    }
}
