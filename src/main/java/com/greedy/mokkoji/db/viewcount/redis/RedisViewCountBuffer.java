package com.greedy.mokkoji.db.viewcount.redis;

import com.greedy.mokkoji.api.viewcount.model.ViewCountBatch;
import com.greedy.mokkoji.api.viewcount.model.ViewCountBucket;
import com.greedy.mokkoji.common.exception.MokkojiException;
import com.greedy.mokkoji.config.ViewCountProperties;
import com.greedy.mokkoji.enums.message.FailMessage;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Repository;

import java.time.Clock;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Repository
public class RedisViewCountBuffer {

    private static final String KEY_DELIMITER = ":";
    private static final String SEEN_KEY_SUFFIX = ":seen:";
    private static final String ACTIVE_KEY_SUFFIX = ":active";
    private static final String PENDING_INDEX_KEY_SUFFIX = ":pending-index";
    private static final String BATCH_KEY_SUFFIX = ":batch:";
    private static final String SCRIPT_DIRECTORY = "redis/";
    private static final long SCRIPT_SUCCESS_RESULT = 1L;

    private static final DefaultRedisScript<Long> RECORD_VIEW_SCRIPT = createRedisScript("view-count-deduplicate-and-increment.lua");
    private static final DefaultRedisScript<Long> CREATE_PENDING_BATCH_SCRIPT = createRedisScript("view-count-create-pending-batch.lua");
    private static final DefaultRedisScript<Long> DELETE_PROCESSED_BATCH_SCRIPT = createRedisScript("view-count-delete-applied-batch.lua");

    private final StringRedisTemplate viewCountRedisTemplate;
    private final ViewCountProperties viewCountProperties;
    private final Clock viewCountClock;

    public RedisViewCountBuffer(
            @Qualifier("viewCountRedisTemplate") final StringRedisTemplate viewCountRedisTemplate,
            final ViewCountProperties viewCountProperties,
            @Qualifier("viewCountClock") final Clock viewCountClock
    ) {
        this.viewCountRedisTemplate = viewCountRedisTemplate;
        this.viewCountProperties = viewCountProperties;
        this.viewCountClock = viewCountClock;
    }

    public boolean recordView(final ViewCountBucket bucket, final String visitorKey, final Duration dedupTtl) {
        if (bucket == null || visitorKey == null || visitorKey.isBlank()
                || dedupTtl == null || dedupTtl.toMillis() <= 0) {
            throw new MokkojiException(FailMessage.INTERNAL_SERVER_ERROR_VIEW_COUNT_DATA);
        }

        final String seenKey = getKeyPrefix() + SEEN_KEY_SUFFIX + bucket.getDedupScope() + KEY_DELIMITER + visitorKey;
        final long result = executeRedisScript(
                RECORD_VIEW_SCRIPT,
                List.of(getActiveKey(), seenKey),
                bucket.getRedisField(),
                Long.toString(dedupTtl.toMillis())
        );
        return result == SCRIPT_SUCCESS_RESULT;
    }

    public Optional<String> createPendingBatch() {
        final String batchId = UUID.randomUUID().toString();
        final long result = executeRedisScript(
                CREATE_PENDING_BATCH_SCRIPT,
                List.of(getActiveKey(), getBatchKey(batchId), getPendingIndexKey()),
                batchId,
                Long.toString(viewCountClock.millis()),
                Integer.toString(viewCountProperties.getMaxPendingBatches())
        );
        return result == SCRIPT_SUCCESS_RESULT ? Optional.of(batchId) : Optional.empty();
    }

    public List<String> getPendingBatchIds(final int limit) {
        if (limit <= 0) {
            return List.of();
        }

        final Set<String> batchIds = viewCountRedisTemplate.opsForZSet().range(getPendingIndexKey(), 0, limit - 1L);
        return batchIds == null ? List.of() : List.copyOf(batchIds);
    }

    public ViewCountBatch readBatch(final String batchId) {

        final String batchKey = getBatchKey(batchId);
        final Map<ViewCountBucket, Long> deltas = new HashMap<>();
        final Map<String, String> entries = viewCountRedisTemplate.<String, String>opsForHash().entries(batchKey);

        entries.forEach((field, delta) -> {
            deltas.put(
                    ViewCountBucket.parseRedisField(field),
                    Long.parseLong(delta)
            );
        });

        if (deltas.isEmpty()) {
            throw new MokkojiException(FailMessage.INTERNAL_SERVER_ERROR_VIEW_COUNT_BATCH_NOT_FOUND);
        }

        return new ViewCountBatch(batchId, deltas);
    }

    public void deleteProcessedBatch(final String batchId) {
        executeRedisScript(
                DELETE_PROCESSED_BATCH_SCRIPT,
                List.of(getBatchKey(batchId), getPendingIndexKey()),
                batchId
        );
    }

    private long executeRedisScript(
            final DefaultRedisScript<Long> script,
            final List<String> keys,
            final Object... arguments
    ) {
        final Long result = viewCountRedisTemplate.execute(script, keys, arguments);
        if (result == null) {
            throw new MokkojiException(FailMessage.INTERNAL_SERVER_ERROR_VIEW_COUNT_REDIS);
        }
        return result;
    }

    private String getKeyPrefix() {
        return viewCountProperties.getKeyPrefix();
    }

    private String getActiveKey() {
        return getKeyPrefix() + ACTIVE_KEY_SUFFIX;
    }

    private String getPendingIndexKey() {
        return getKeyPrefix() + PENDING_INDEX_KEY_SUFFIX;
    }

    private String getBatchKey(final String batchId) {
        if (batchId == null) {
            throw new MokkojiException(FailMessage.INTERNAL_SERVER_ERROR_VIEW_COUNT_DATA);
        }
        return getKeyPrefix() + BATCH_KEY_SUFFIX + batchId;
    }

    private static DefaultRedisScript<Long> createRedisScript(final String scriptFileName) {
        final DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        script.setLocation(new ClassPathResource(SCRIPT_DIRECTORY + scriptFileName));
        script.setResultType(Long.class);
        return script;
    }
}
