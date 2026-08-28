package com.greedy.mokkoji.common.log.query;

import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class QueryMetricsRecorder {

    private static final String QUERY_COUNT_METRIC = "request.db.query.count";
    private static final String QUERY_TIME_METRIC = "request.db.query.time";

    private final MeterRegistry meterRegistry;

    public void record(final String method, final String uriPattern, final long queryCount, final long queryTimeNanos) {
        DistributionSummary.builder(QUERY_COUNT_METRIC)
                .description("요청당 DB 쿼리 수")
                .tag("method", method)
                .tag("uri", uriPattern)
                .register(meterRegistry)
                .record(queryCount);

        Timer.builder(QUERY_TIME_METRIC)
                .description("요청당 DB 쿼리 실행 시간 합")
                .tag("method", method)
                .tag("uri", uriPattern)
                .register(meterRegistry)
                .record(queryTimeNanos, TimeUnit.NANOSECONDS);
    }
}
