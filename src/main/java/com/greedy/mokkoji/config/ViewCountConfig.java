package com.greedy.mokkoji.config;

import io.lettuce.core.ClientOptions;
import io.lettuce.core.SocketOptions;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import java.time.Clock;
import java.time.Duration;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Configuration
@EnableConfigurationProperties(ViewCountProperties.class)
public class ViewCountConfig {

    @Bean
    public LettuceConnectionFactory viewCountRedisConnectionFactory(
            final ViewCountProperties viewCountProperties,
            @Value("${view-count.redis-host:${VIEW_COUNT_REDIS_HOST:${redis.host}}}") final String host,
            @Value("${view-count.redis-port:${VIEW_COUNT_REDIS_PORT:${redis.port}}}") final int port
    ) {
        final ClientOptions clientOptions = ClientOptions.builder()
                .disconnectedBehavior(ClientOptions.DisconnectedBehavior.REJECT_COMMANDS)
                .requestQueueSize(256)
                .socketOptions(SocketOptions.builder()
                        .connectTimeout(viewCountProperties.getCommandTimeout())
                        .build())
                .build();
        final LettuceClientConfiguration clientConfiguration = LettuceClientConfiguration.builder()
                .commandTimeout(viewCountProperties.getCommandTimeout())
                .shutdownTimeout(Duration.ZERO)
                .clientOptions(clientOptions)
                .build();
        return new LettuceConnectionFactory(new RedisStandaloneConfiguration(host, port), clientConfiguration);
    }

    @Bean
    public StringRedisTemplate viewCountRedisTemplate(
            @Qualifier("viewCountRedisConnectionFactory") final LettuceConnectionFactory connectionFactory
    ) {
        return new StringRedisTemplate(connectionFactory);
    }

    @Bean
    public Clock viewCountClock() {
        return Clock.systemUTC();
    }

    @Bean(destroyMethod = "shutdownNow")
    public ExecutorService viewCountRecordExecutor() {
        // 연결 대기를 포함한 호출 시간을 제한하고, 장애 시 대기 작업이 무제한으로 쌓이지 않게 한다.
        return new ThreadPoolExecutor(
                4,
                4,
                0,
                TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<>(128),
                Thread.ofPlatform().daemon(true).name("view-count-record-", 0).factory(),
                new ThreadPoolExecutor.AbortPolicy()
        );
    }

    @Bean
    public ThreadPoolTaskScheduler viewCountScheduler() {
        final ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(1);
        scheduler.setThreadNamePrefix("view-count-flush-");
        scheduler.setWaitForTasksToCompleteOnShutdown(true);
        scheduler.setAwaitTerminationSeconds(10);
        return scheduler;
    }
}
