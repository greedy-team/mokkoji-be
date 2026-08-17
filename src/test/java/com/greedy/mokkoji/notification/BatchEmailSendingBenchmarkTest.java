package com.greedy.mokkoji.notification;

import com.greedy.mokkoji.api.email.service.DiscordNotifier;
import com.greedy.mokkoji.api.email.service.RecruitmentMailPayload;
import com.greedy.mokkoji.api.email.service.RecruitmentNotificationEmailChannel;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadPoolExecutor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;

/**
 * async 개별발송(club당 send 1회) vs async+배치발송(청크당 send 1회)의 성능 차이를 수치로 측정하는 벤치마크.
 *
 * 실측값(MeasureSmtpLatency, Gmail SMTP 10회):
 *   H(핸드셰이크): 2,470ms  — send() 1회당 연결·TLS·인증·종료 비용
 *   S(건당 전송):  1,668ms  — 메시지 자체 전송 비용 (배치 내 건당 동일)
 *
 * 두 지표로 모델 예측:
 *   개별발송 wall time = CEIL(N / POOL) × (H + S) = 4 × 4,138 ≈ 16,552ms
 *   배치발송 wall time ≈ H + CHUNK_SIZE × S  = 2,470 + 5 × 1,668 ≈ 10,810ms
 *     → 실측 9,149ms (모델 9,142ms)와 일치
 *
 * 이 테스트는 실제 SMTP 발송 없이 send() 지연만 주입해 구조적 효과를 검증한다.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("이메일 배치 발송 벤치마크 (async 개별 vs async+배치)")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class BatchEmailSendingBenchmarkTest {

    /** send() 1회당 핸드셰이크 비용 (실측 median, ms). */
    private static final long H = 2_470L;
    /** 배치 내 건당 전송 비용 (실측값, ms). */
    private static final long S = 1_668L;
    /** 동아리(=발송 건) 수. */
    private static final int CLUB_COUNT = 20;
    /** 청크 수 = emailExecutor corePoolSize. 항상 모든 스레드를 활용하기 위해 개수를 고정. */
    private static final int CHUNK_COUNT = 5;

    private static final int CORE_POOL_SIZE = 5;
    private static final int MAX_POOL_SIZE = 10;
    private static final int QUEUE_CAPACITY = 300;

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private DiscordNotifier discordNotifier;

    private RecruitmentNotificationEmailChannel channel;
    private List<RecruitmentMailPayload> payloads;

    @BeforeEach
    void setUp() {
        channel = new RecruitmentNotificationEmailChannel(mailSender, discordNotifier);
        ReflectionTestUtils.setField(channel, "senderMail", "test@mokkoji.com");
        ReflectionTestUtils.setField(channel, "baseUrl", "https://mokkoji.com");
        ReflectionTestUtils.setField(channel, "mailBannerUrl", "https://mokkoji.com/banner.png");

        BDDMockito.given(mailSender.createMimeMessage())
                .willAnswer(inv -> new MimeMessage((Session) null));

        payloads = new ArrayList<>();
        for (int i = 0; i < CLUB_COUNT; i++) {
            payloads.add(new RecruitmentMailPayload(
                    (long) i, "동아리" + i,
                    List.of("user" + i + "@test.com"),
                    LocalDateTime.now(), LocalDateTime.now().plusDays(7)
            ));
        }
    }

    @Test
    @DisplayName("async 개별발송: club당 커넥션 1개, 풀 포화 시 직렬 누적")
    void async_개별발송() throws InterruptedException {
        // send(MimeMessage) 1회 = H + S (매번 새 핸드셰이크)
        BDDMockito.willAnswer(inv -> { Thread.sleep(H + S); return null; })
                .given(mailSender).send(any(MimeMessage.class));

        ThreadPoolTaskExecutor executor = buildEmailExecutor();
        CountDownLatch done = new CountDownLatch(CLUB_COUNT);

        long start = System.currentTimeMillis();

        for (RecruitmentMailPayload payload : payloads) {
            executor.execute(() -> {
                try {
                    MimeMessage msg = mailSender.createMimeMessage();
                    mailSender.send(msg);
                } finally {
                    done.countDown();
                }
            });
        }

        done.await();
        long totalMs = System.currentTimeMillis() - start;
        executor.shutdown();

        long expectedMin = (long) Math.ceil((double) CLUB_COUNT / CORE_POOL_SIZE) * (H + S);
        System.out.printf("[개별발송] 전체 완료 = %dms  (예측 ≥ %dms, 커넥션 %d개)%n",
                totalMs, expectedMin, CLUB_COUNT);

        // CEIL(N/POOL) 라운드만큼 H+S가 직렬 누적된다
        assertThat(totalMs).isGreaterThanOrEqualTo(expectedMin);
    }

    @Test
    @DisplayName("async+배치발송: 청크당 커넥션 1개, 핸드셰이크 상각")
    void async_배치발송() throws InterruptedException {
        // send(MimeMessage[N]) 1회 = H + N×S (핸드셰이크 1번만)
        // Mockito는 varargs를 개별 인수로 언팩하므로 getArguments().length가 메시지 수
        BDDMockito.willAnswer(inv -> {
            int msgCount = inv.getArguments().length;
            Thread.sleep(H + (long) msgCount * S);
            return null;
        }).given(mailSender).send(any(MimeMessage[].class));

        List<List<RecruitmentMailPayload>> chunks = partitionByCount(payloads, CHUNK_COUNT);
        ThreadPoolTaskExecutor executor = buildEmailExecutor();
        CountDownLatch done = new CountDownLatch(chunks.size());

        long start = System.currentTimeMillis();

        for (List<RecruitmentMailPayload> chunk : chunks) {
            executor.execute(() -> {
                try {
                    channel.sendBatchNotification(chunk); // @Async 미적용 — 직접 동기 호출
                } finally {
                    done.countDown();
                }
            });
        }

        done.await();
        long totalMs = System.currentTimeMillis() - start;
        executor.shutdown();

        int clubsPerChunk = CLUB_COUNT / CHUNK_COUNT;
        long chunkTime = H + (long) clubsPerChunk * S;
        long oldApproachMin = (long) Math.ceil((double) CLUB_COUNT / CORE_POOL_SIZE) * (H + S);
        System.out.printf("[배치발송] 전체 완료 = %dms  (예측 ≈ %dms, 커넥션 %d개)%n",
                totalMs, chunkTime, chunks.size());
        System.out.printf("  → 개별발송 대비 %.1f배 단축 (모델 예측 %.1f배)%n",
                (double) oldApproachMin / totalMs,
                (double) oldApproachMin / chunkTime);

        // 모든 청크가 병렬 실행되므로 wall time ≈ H + (N/CHUNK_COUNT)×S
        // 개별발송 최솟값보다 확실히 빠르다
        assertThat(totalMs).isLessThan(oldApproachMin);
        // 커넥션 수 = CHUNK_COUNT (= 풀 크기)
        assertThat(chunks.size()).isEqualTo(CHUNK_COUNT);
    }

    /** payloads를 count개 청크로 균등 분할. */
    private List<List<RecruitmentMailPayload>> partitionByCount(List<RecruitmentMailPayload> list, int count) {
        int actualCount = Math.min(count, list.size());
        List<List<RecruitmentMailPayload>> result = new ArrayList<>();
        for (int i = 0; i < actualCount; i++) {
            int from = i * list.size() / actualCount;
            int to = (i + 1) * list.size() / actualCount;
            result.add(list.subList(from, to));
        }
        return result;
    }

    private ThreadPoolTaskExecutor buildEmailExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(CORE_POOL_SIZE);
        executor.setMaxPoolSize(MAX_POOL_SIZE);
        executor.setQueueCapacity(QUEUE_CAPACITY);
        executor.setThreadNamePrefix("email-async-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }
}
