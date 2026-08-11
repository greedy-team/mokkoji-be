package com.greedy.mokkoji.concurrency;

import com.greedy.mokkoji.api.club.service.ClubService;
import com.greedy.mokkoji.common.AbstractTest;
import com.greedy.mokkoji.common.fixture.Fixture;
import com.greedy.mokkoji.db.club.entity.Club;
import com.greedy.mokkoji.db.university.entity.University;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 조회수 v0(read-modify-write, 더티체킹)의 lost update 재현 테스트.
 *
 * 가정한 상황: 인기 동아리 상세 페이지에 여러 사용자가 "동시에" 진입한다.
 * 각 요청은 같은 club 행을 읽고(view_count=n), 메모리에서 +1 한 뒤(n+1),
 * 커밋 시점에 UPDATE 한다. 두 요청이 같은 n을 읽으면 둘 다 n+1을 쓰므로
 * 증가 1회가 유실된다(lost update).
 *
 * v0에서 이 테스트는 실패해야 하며(유실 실증), v1(원자 UPDATE) 적용 후
 * 통과하여 회귀 방지 테스트로 남는다.
 */
@SpringBootTest
class ViewCountRaceTest extends AbstractTest {

    private static final int VIEW_THREADS = 100;

    @Autowired
    private ClubService clubService;

    @AfterEach
    void tearDown() {
        recruitmentImageRepository.deleteAll();
        recruitmentRepository.deleteAll();
        commentRepository.deleteAll();
        favoriteRepository.deleteAll();
        clubRepository.deleteAll();
        userRepository.deleteAll();
        universityRepository.deleteAll();
    }

    @Test
    @DisplayName("동시 조회 100건이면 조회수는 정확히 100 증가해야 한다")
    void viewCount_underConcurrency_increasesExactly() throws Exception {
        final University university = universityRepository.save(Fixture.createUniversity());
        final Club club = clubRepository.save(Fixture.createClub(university));

        final RaceResult result = runConcurrently(VIEW_THREADS,
                () -> clubService.findClub(null, club.getId()));

        final Club after = clubRepository.findById(club.getId()).orElseThrow();
        final long lost = VIEW_THREADS - after.getViewCount();

        System.out.printf("[VIEWCOUNT-RACE] threads=%d success=%d failed=%d viewCount=%d lost=%d%n",
                VIEW_THREADS, result.success(), result.failed(), after.getViewCount(), lost);

        assertThat(after.getViewCount())
                .as("동시 조회 %d건(성공 응답 %d건) 후 view_count", VIEW_THREADS, result.success())
                .isEqualTo((long) VIEW_THREADS);
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

        ready.await();
        start.countDown();
        done.await(60, TimeUnit.SECONDS);
        executor.shutdownNow();

        return new RaceResult(success.get(), failed.get());
    }

    private record RaceResult(int success, int failed) {
    }
}
