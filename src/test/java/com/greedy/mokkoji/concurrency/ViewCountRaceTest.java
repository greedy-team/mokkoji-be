package com.greedy.mokkoji.concurrency;

import com.greedy.mokkoji.api.viewcount.model.ViewCountBatch;
import com.greedy.mokkoji.api.viewcount.model.ViewCountBucket;
import com.greedy.mokkoji.api.viewcount.service.ViewCountBatchApplier;
import com.greedy.mokkoji.common.AbstractTest;
import com.greedy.mokkoji.common.fixture.Fixture;
import com.greedy.mokkoji.db.club.entity.Club;
import com.greedy.mokkoji.db.university.entity.University;
import com.greedy.mokkoji.db.viewcount.repository.ViewCountRepository;
import com.greedy.mokkoji.enums.viewcount.ViewCountTarget;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 같은 동아리의 조회수 배치를 동시에 DB에 반영할 때 증가분 유실과 중복 합산을 검증한다.
 * Redis 수집과 상세 GET은 제외하고 실제 MySQL 트랜잭션을 사용하는 테스트다.
 */
@SpringBootTest(properties = "view-count.flush-enabled=false")
class ViewCountRaceTest extends AbstractTest {

    private static final int BATCH_THREADS = 100;

    @Autowired
    private ViewCountBatchApplier viewCountBatchApplier;

    @Autowired
    private ViewCountRepository viewCountRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @AfterEach
    void tearDown() {
        jdbcTemplate.update("DELETE FROM view_count_batch_history");
        jdbcTemplate.update("DELETE FROM view_count");
        recruitmentImageRepository.deleteAll();
        recruitmentRepository.deleteAll();
        commentRepository.deleteAll();
        favoriteRepository.deleteAll();
        clubRepository.deleteAll();
        userRepository.deleteAll();
        universityRepository.deleteAll();
    }

    @Test
    @DisplayName("서로 다른 조회수 배치 100개를 동시에 반영하면 증가분이 모두 합산된다")
    void applyDifferentBatchesConcurrently() throws Exception {
        final University university = universityRepository.save(Fixture.createUniversity());
        final Club club = clubRepository.save(Fixture.createClub(university));
        final ViewCountBucket bucket = new ViewCountBucket(ViewCountTarget.CLUB, club.getId(), LocalDate.of(2026, 11, 24));

        final RaceResult result = runConcurrently(BATCH_THREADS,
                () -> viewCountBatchApplier.applyBatch(new ViewCountBatch(UUID.randomUUID().toString(), Map.of(bucket, 1L))));

        assertThat(result.failed()).isZero();
        assertThat(result.success()).isEqualTo(BATCH_THREADS);
        assertThat(viewCountRepository.findClubViewCount(club.getId())).isEqualTo(BATCH_THREADS);
    }

    @Test
    @DisplayName("같은 조회수 배치를 동시에 재처리해도 한 번만 합산된다")
    void applySameBatchConcurrently() throws Exception {
        final University university = universityRepository.save(Fixture.createUniversity());
        final Club club = clubRepository.save(Fixture.createClub(university));
        final ViewCountBucket bucket = new ViewCountBucket(ViewCountTarget.CLUB, club.getId(), LocalDate.of(2026, 11, 24));
        final ViewCountBatch batch = new ViewCountBatch(UUID.randomUUID().toString(), Map.of(bucket, 5L));

        final RaceResult result = runConcurrently(BATCH_THREADS, () -> viewCountBatchApplier.applyBatch(batch));

        assertThat(result.failed()).isZero();
        assertThat(result.success()).isEqualTo(BATCH_THREADS);
        assertThat(viewCountRepository.findClubViewCount(club.getId())).isEqualTo(5L);
        assertThat(viewCountRepository.existsBatchHistory(batch.id())).isTrue();
    }

    private RaceResult runConcurrently(final int threadCount, final Runnable task) throws InterruptedException {
        final ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        final CountDownLatch ready = new CountDownLatch(threadCount);
        final CountDownLatch start = new CountDownLatch(1);
        final CountDownLatch done = new CountDownLatch(threadCount);
        final AtomicInteger success = new AtomicInteger();
        final AtomicInteger failed = new AtomicInteger();

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                ready.countDown();
                try {
                    start.await();
                    task.run();
                    success.incrementAndGet();
                } catch (Exception e) {
                    failed.incrementAndGet();
                } finally {
                    done.countDown();
                }
            });
        }

        try {
            assertThat(ready.await(10, TimeUnit.SECONDS)).as("배치 처리 스레드 준비").isTrue();
            start.countDown();
            assertThat(done.await(60, TimeUnit.SECONDS)).as("배치 처리 완료").isTrue();
        } finally {
            start.countDown();
            executor.shutdownNow();
        }

        return new RaceResult(success.get(), failed.get());
    }

    private record RaceResult(int success, int failed) {
    }
}
