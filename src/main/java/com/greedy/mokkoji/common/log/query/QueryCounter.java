package com.greedy.mokkoji.common.log.query;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

@Getter
@Component
@RequestScope
@RequiredArgsConstructor
public class QueryCounter {
    private final Long time = System.currentTimeMillis();
    private Long count = 0L;
    private Long successQueryTimeNanos = 0L;
    private Long failedQueryTimeNanos = 0L;

    public void increaseCount() {
        count++;
    }

    public void addSuccessQueryTime(final long nanos) {
        successQueryTimeNanos += nanos;
    }

    public void addFailedQueryTime(final long nanos) {
        failedQueryTimeNanos += nanos;
    }
}
