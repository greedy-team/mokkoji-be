package com.greedy.mokkoji.api.viewcount.service;

import com.greedy.mokkoji.api.viewcount.model.ViewCountBatch;
import com.greedy.mokkoji.common.exception.MokkojiException;
import com.greedy.mokkoji.config.ViewCountProperties;
import com.greedy.mokkoji.db.viewcount.redis.RedisViewCountBuffer;
import com.greedy.mokkoji.enums.message.FailMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ViewCountFlushWorker {

    private final RedisViewCountBuffer redisViewCountBuffer;
    private final ViewCountBatchApplier viewCountBatchApplier;
    private final ViewCountProperties viewCountProperties;

    public void flushViewCounts() {
        createPendingBatch();
        processPendingBatches();
    }

    private void createPendingBatch() {
        try {
            redisViewCountBuffer.createPendingBatch();
        } catch (RuntimeException exception) {
            logFlushFailure("VIEW COUNT BATCH CREATE FAILED", null, exception);
        }
    }

    private void processPendingBatches() {
        final List<String> batchIds;
        try {
            batchIds = redisViewCountBuffer.getPendingBatchIds(viewCountProperties.getMaxBatchesPerRun());
        } catch (RuntimeException exception) {
            logFlushFailure("VIEW COUNT BATCH LIST FAILED", null, exception);
            return;
        }

        for (final String batchId : batchIds) {
            applyAndDeleteBatch(batchId);
        }
    }

    private void applyAndDeleteBatch(final String batchId) {
        try {
            final ViewCountBatch batch = redisViewCountBuffer.readBatch(batchId);
            viewCountBatchApplier.applyBatch(batch);
            redisViewCountBuffer.deleteProcessedBatch(batchId);
        } catch (RuntimeException exception) {
            logFlushFailure("VIEW COUNT BATCH PROCESS FAILED", batchId, exception);
        }
    }

    private void logFlushFailure(final String event, final String batchId, final RuntimeException exception) {
        final FailMessage failMessage = exception instanceof MokkojiException mokkojiException
                ? mokkojiException.getFailMessage() : FailMessage.INTERNAL_SERVER_ERROR;

        log.warn("[{}] batchId={}, 실패 코드: {}, 실패 메세지: {}",
                event, batchId, failMessage.getCode(), failMessage.getMessage(), exception);
    }
}
