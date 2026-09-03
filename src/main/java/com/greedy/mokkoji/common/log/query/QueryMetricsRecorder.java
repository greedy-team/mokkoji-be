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
    private static final double[] PERCENTILES = {0.5, 0.95, 0.99};
    private static final String SUCCESS_STATUS = "success";
    private static final String FAILURE_STATUS = "failure";

    private final MeterRegistry meterRegistry;

    public void record(
            final String method,
            final String uriPattern,
            final long queryCount,
            final long successQueryTimeNanos,
            final long failedQueryTimeNanos
    ) {
        DistributionSummary.builder(QUERY_COUNT_METRIC)
                .description("요청당 DB 쿼리 수")
                .tag("method", method)
                .tag("uri", uriPattern)
                .publishPercentiles(PERCENTILES)
                .register(meterRegistry)
                .record(queryCount);

        recordQueryTime(method, uriPattern, SUCCESS_STATUS, successQueryTimeNanos);
        recordQueryTime(method, uriPattern, FAILURE_STATUS, failedQueryTimeNanos);
    }

    private void recordQueryTime(final String method, final String uriPattern, final String status, final long nanos) {
        if (nanos == 0) {
            return;
        }

        Timer.builder(QUERY_TIME_METRIC)
                .description("요청당 DB 쿼리 실행 시간 합")
                .tag("method", method)
                .tag("uri", uriPattern)
                .tag("status", status)
                .publishPercentiles(PERCENTILES)
                .register(meterRegistry)
                .record(nanos, TimeUnit.NANOSECONDS);
    }
}
