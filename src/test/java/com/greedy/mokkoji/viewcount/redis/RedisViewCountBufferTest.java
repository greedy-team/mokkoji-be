package com.greedy.mokkoji.viewcount.redis;

import com.greedy.mokkoji.api.viewcount.model.ViewCountBucket;
import com.greedy.mokkoji.config.ViewCountProperties;
import com.greedy.mokkoji.db.viewcount.redis.RedisViewCountBuffer;
import com.greedy.mokkoji.enums.viewcount.ViewCountTarget;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Clock;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@ExtendWith(OutputCaptureExtension.class)
@DisplayName("조회수 배치 생성 테스트")
class RedisViewCountBufferTest {

    @Container
    private static final GenericContainer<?> REDIS = new GenericContainer<>("redis:7.4.2-alpine")
            .withExposedPorts(6379);

    private static LettuceConnectionFactory connectionFactory;
    private static StringRedisTemplate redisTemplate;

    private ViewCountProperties properties;
    private RedisViewCountBuffer buffer;
    private String activeKey;
    private String pendingIndexKey;

    private final ViewCountBucket bucket = new ViewCountBucket(ViewCountTarget.CLUB, 42L, LocalDate.of(2026, 11, 24));

    @BeforeAll
    static void connectRedis() {
        connectionFactory = new LettuceConnectionFactory(REDIS.getHost(), REDIS.getMappedPort(6379));
        connectionFactory.afterPropertiesSet();
        redisTemplate = new StringRedisTemplate(connectionFactory);
    }

    @AfterAll
    static void disconnectRedis() {
        if (connectionFactory != null) {
            connectionFactory.destroy();
        }
    }

    @BeforeEach
    void setUp() {
        properties = new ViewCountProperties();
        properties.setKeyPrefix("test:view-count:" + UUID.randomUUID());
        buffer = new RedisViewCountBuffer(redisTemplate, properties, Clock.systemUTC());
        activeKey = properties.getKeyPrefix() + ":active";
        pendingIndexKey = properties.getKeyPrefix() + ":pending-index";
    }

    @Test
    @DisplayName("증가분이 없으면 배치를 생성하거나 한도 경고를 남기지 않는다")
    void skipEmptyActiveWithoutWarning(final CapturedOutput output) {
        assertThat(buffer.createPendingBatch()).isEmpty();
        assertThat(redisTemplate.hasKey(pendingIndexKey)).isFalse();
        assertThat(output.getAll()).doesNotContain("[VIEW COUNT BATCH LIMIT REACHED]");
    }

    @Test
    @DisplayName("대기 한도에 도달하면 경고를 남기고 증가분을 보존하며 여유가 생기면 배치로 옮긴다")
    void warnAndKeepDeltasUntilPendingCapacityAvailable(final CapturedOutput output) {
        final String firstBatchId = UUID.randomUUID().toString();
        redisTemplate.opsForZSet().add(pendingIndexKey, firstBatchId, 0);
        for (int index = 1; index < properties.getMaxPendingBatches(); index++) {
            redisTemplate.opsForZSet().add(pendingIndexKey, UUID.randomUUID().toString(), index);
        }
        redisTemplate.opsForHash().put(activeKey, bucket.getRedisField(), "5");

        assertThat(buffer.createPendingBatch()).isEmpty();

        assertThat(output.getAll()).contains("[VIEW COUNT BATCH LIMIT REACHED]", "256");
        assertThat(redisTemplate.opsForHash().get(activeKey, bucket.getRedisField())).isEqualTo("5");
        assertThat(redisTemplate.opsForZSet().size(pendingIndexKey)).isEqualTo(properties.getMaxPendingBatches());

        assertThat(buffer.recordView(bucket, "anonymous:new-visitor", properties.getDedupTtl())).isTrue();
        redisTemplate.opsForZSet().remove(pendingIndexKey, firstBatchId);
        final String batchId = buffer.createPendingBatch().orElseThrow();

        assertThat(buffer.readBatch(batchId).deltas()).containsEntry(bucket, 6L);
        assertThat(redisTemplate.hasKey(activeKey)).isFalse();
        assertThat(redisTemplate.opsForZSet().size(pendingIndexKey)).isEqualTo(properties.getMaxPendingBatches());
        assertThat(output.getAll().lines().filter(line -> line.contains("[VIEW COUNT BATCH LIMIT REACHED]")).count()).isEqualTo(1L);
    }

    @Test
    @DisplayName("대기 한도 미만이면 경고 없이 증가분을 배치에 보관하고 대기 목록에 등록한다")
    void createPendingBatchWithoutWarning(final CapturedOutput output) {
        redisTemplate.opsForHash().put(activeKey, bucket.getRedisField(), "5");

        final String batchId = buffer.createPendingBatch().orElseThrow();

        assertThat(buffer.readBatch(batchId).deltas()).containsEntry(bucket, 5L);
        assertThat(buffer.getPendingBatchIds(properties.getMaxBatchesPerRun())).containsExactly(batchId);
        assertThat(redisTemplate.hasKey(activeKey)).isFalse();
        assertThat(output.getAll()).doesNotContain("[VIEW COUNT BATCH LIMIT REACHED]");
    }
}
