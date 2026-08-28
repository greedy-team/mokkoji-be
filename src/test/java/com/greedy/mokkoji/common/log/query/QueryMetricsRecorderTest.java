package com.greedy.mokkoji.common.log.query;

import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("쿼리 메트릭 기록 테스트")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
public class QueryMetricsRecorderTest {

    private final SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
    private final QueryMetricsRecorder queryMetricsRecorder = new QueryMetricsRecorder(meterRegistry);

    @Test
    @DisplayName("요청당 쿼리 수와 쿼리 시간이 method/uri 태그로 기록된다")
    void recordQueryCountAndTime() {
        //given & when
        queryMetricsRecorder.record("GET", "/api/clubs/search", 56, 1_000_000L);
        queryMetricsRecorder.record("GET", "/api/clubs/search", 4, 500_000L);

        //then
        final DistributionSummary summary = meterRegistry.find("request.db.query.count")
                .tag("method", "GET")
                .tag("uri", "/api/clubs/search")
                .summary();
        assertThat(summary).isNotNull();
        assertThat(summary.count()).isEqualTo(2);
        assertThat(summary.totalAmount()).isEqualTo(60);
        assertThat(summary.max()).isEqualTo(56);

        final Timer timer = meterRegistry.find("request.db.query.time")
                .tag("method", "GET")
                .tag("uri", "/api/clubs/search")
                .timer();
        assertThat(timer).isNotNull();
        assertThat(timer.count()).isEqualTo(2);
        assertThat(timer.totalTime(TimeUnit.NANOSECONDS)).isEqualTo(1_500_000L);
    }

    @Test
    @DisplayName("서로 다른 uri는 별도 시계열로 기록된다")
    void recordSeparateTimeSeriesPerUri() {
        //given & when
        queryMetricsRecorder.record("GET", "/api/clubs/search", 10, 100L);
        queryMetricsRecorder.record("GET", "/api/clubs/{clubId}", 3, 100L);

        //then
        final DistributionSummary searchSummary = meterRegistry.find("request.db.query.count")
                .tag("uri", "/api/clubs/search")
                .summary();
        final DistributionSummary detailSummary = meterRegistry.find("request.db.query.count")
                .tag("uri", "/api/clubs/{clubId}")
                .summary();

        assertThat(searchSummary).isNotNull();
        assertThat(searchSummary.totalAmount()).isEqualTo(10);
        assertThat(detailSummary).isNotNull();
        assertThat(detailSummary.totalAmount()).isEqualTo(3);
    }
}
