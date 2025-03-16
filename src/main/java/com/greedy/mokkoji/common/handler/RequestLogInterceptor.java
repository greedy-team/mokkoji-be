package com.greedy.mokkoji.common.handler;

import com.greedy.mokkoji.common.log.QueryCounter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
@Component
@RequiredArgsConstructor
public class RequestLogInterceptor implements HandlerInterceptor {

    private static final int QUERY_COUNT_WARNING_STANDARD = 10;
    private static final int TOTAL_TIME_WARNING_STANDARD_MS = 2500;
    private final QueryCounter queryCounter;

    @Override
    public void afterCompletion(
            final HttpServletRequest request,
            final HttpServletResponse response,
            final Object handler,
            final Exception ex
    ) {
        final Long queryCount = queryCounter.getCount();
        final double duration = (System.currentTimeMillis() - queryCounter.getTime()) / 1000.0;

        warnAboutQuery(queryCount, duration);
    }

    private void warnAboutQuery(Long queryCount, double duration) {
        if (queryCount > QUERY_COUNT_WARNING_STANDARD) {
            log.warn("하나의 요청에 쿼리가 10번 이상 날라 갔습니다. 쿼리 횟수: {}", queryCount);
        }

        if (duration > TOTAL_TIME_WARNING_STANDARD_MS) {
            log.warn("하나의 요청이 0.25ms 이상 소요되었습니다. 실제 소요 시간: {}", duration);
        }
    }
}
