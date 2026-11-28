package com.greedy.mokkoji.api.viewcount.service;

import com.greedy.mokkoji.api.viewcount.model.ViewCountBucket;
import com.greedy.mokkoji.common.exception.MokkojiException;
import com.greedy.mokkoji.config.ViewCountProperties;
import com.greedy.mokkoji.db.viewcount.redis.RedisViewCountBuffer;
import com.greedy.mokkoji.enums.message.FailMessage;
import com.greedy.mokkoji.enums.viewcount.ViewCountTarget;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Service
public class ClubViewRecorder {

    private static final ZoneId STATISTICS_ZONE_ID = ZoneId.of("Asia/Seoul");

    private final RedisViewCountBuffer redisViewCountBuffer;
    private final ViewCountProperties viewCountProperties;
    private final Clock viewCountClock;
    private final ExecutorService viewCountRecordExecutor;

    private final AtomicLong retryAfterMillis = new AtomicLong();

    public ClubViewRecorder(
            final RedisViewCountBuffer redisViewCountBuffer,
            final ViewCountProperties viewCountProperties,
            @Qualifier("viewCountClock") final Clock viewCountClock,
            @Qualifier("viewCountRecordExecutor") final ExecutorService viewCountRecordExecutor
    ) {
        this.redisViewCountBuffer = redisViewCountBuffer;
        this.viewCountProperties = viewCountProperties;
        this.viewCountClock = viewCountClock;
        this.viewCountRecordExecutor = viewCountRecordExecutor;
    }

    public void recordClubView(final long clubId, final String visitorKey) {

        if (!viewCountProperties.isEnabled() || viewCountClock.millis() < retryAfterMillis.get()) {
            return;
        }

        try {
            recordViewWithinTimeout(clubId, visitorKey);
        }
        catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            handleRecordFailure(exception);
        }
        catch (ExecutionException | TimeoutException | RuntimeException exception) {
            handleRecordFailure(exception);
        }
    }

    private void recordViewWithinTimeout(final long clubId, final String visitorKey) throws InterruptedException, ExecutionException, TimeoutException {
        final long startedAtNanos = System.nanoTime();
        final ViewCountBucket viewCountBucket = new ViewCountBucket(
                ViewCountTarget.CLUB,
                clubId,
                LocalDate.ofInstant(viewCountClock.instant(), STATISTICS_ZONE_ID)
        );

        final Future<Boolean> recordTask = viewCountRecordExecutor.submit(
                () -> redisViewCountBuffer.recordView(viewCountBucket, visitorKey, viewCountProperties.getDedupTtl())
        );

        try {
            final long remainingNanos = viewCountProperties.getCommandTimeout().toNanos() - (System.nanoTime() - startedAtNanos);
            if (remainingNanos <= 0) {
                throw new MokkojiException(FailMessage.INTERNAL_SERVER_ERROR_VIEW_COUNT_RECORD);
            }

            recordTask.get(remainingNanos, TimeUnit.NANOSECONDS);

        } finally {
            if (!recordTask.isDone()) {
                recordTask.cancel(false);
            }
        }
    }

    private void handleRecordFailure(final Exception exception) {
        final long currentTimeMillis = viewCountClock.millis();
        final long previousRetryAfterMillis = retryAfterMillis.getAndSet(
                currentTimeMillis + viewCountProperties.getFailureBackoff().toMillis()
        );
        if (previousRetryAfterMillis > currentTimeMillis) {
            return;
        }

        final Throwable cause = exception instanceof ExecutionException && exception.getCause() != null
                ? exception.getCause() : exception;
        final FailMessage failMessage = cause instanceof MokkojiException mokkojiException
                ? mokkojiException.getFailMessage() : FailMessage.INTERNAL_SERVER_ERROR_VIEW_COUNT_RECORD;

        log.warn("[CLUB VIEW RECORD FAILED] 실패 코드: {}, 실패 메세지: {}", failMessage.getCode(), failMessage.getMessage(), cause);
    }
}
