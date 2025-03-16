package com.greedy.mokkoji.common.handler;

import com.greedy.mokkoji.common.log.CommonLogInformation;
import com.greedy.mokkoji.common.log.query.QueryCounter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.cors.CorsUtils;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
@Component
@RequiredArgsConstructor
public class RequestLogInterceptor implements HandlerInterceptor {

    private static final int QUERY_COUNT_WARNING_STANDARD = 10;
    private static final int TOTAL_TIME_WARNING_STANDARD_MS = 2500;
    private static final double TIME_CONVERSION_MS_TO_SEC = 1000.0;

    private final QueryCounter queryCounter;
    private final CommonLogInformation commonLogInformation;

    @Override
    public boolean preHandle(
            final HttpServletRequest request,
            final HttpServletResponse response,
            final Object handler
    ) throws Exception {
        if (CorsUtils.isPreFlightRequest(request)) {
            return true;
        }

        commonLogInformation.setUri(request.getRequestURI());
        return true;
    }

    @Override
    public void afterCompletion(
            final HttpServletRequest request,
            final HttpServletResponse response,
            final Object handler,
            final Exception ex
    ) {
        final Long queryCount = queryCounter.getCount();
        final double duration = (System.currentTimeMillis() - queryCounter.getTime()) / TIME_CONVERSION_MS_TO_SEC;

        log.info("[{}] URI: {} statusCode: {}",
                commonLogInformation.getRequestIdentifier(), commonLogInformation.getUri(), response.getStatus()
        );
        warnAboutQuery(queryCount, duration);
    }

    private void warnAboutQuery(Long queryCount, double duration) {
        if (queryCount > QUERY_COUNT_WARNING_STANDARD) {
            log.warn("[{}] URI: {}, 하나의 요청에 쿼리가 10번 이상 날라 갔습니다. 쿼리 횟수: {}",
                    commonLogInformation.getRequestIdentifier(), commonLogInformation.getUri(), queryCount
            );
        }

        if (duration > TOTAL_TIME_WARNING_STANDARD_MS) {
            log.warn("[{}] URI: {}, 하나의 요청이 0.25s 이상 소요되었습니다. 실제 소요 시간: {}",
                    commonLogInformation.getRequestIdentifier(), commonLogInformation.getUri(), duration
            );
        }
    }
}
