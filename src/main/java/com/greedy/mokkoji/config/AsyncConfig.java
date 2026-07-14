package com.greedy.mokkoji.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

@Slf4j
@Configuration
@EnableAsync
public class AsyncConfig implements AsyncConfigurer {

    @Bean("emailExecutor")
    public Executor emailExecutor() {
        return buildExecutor("email-async-", 5, 10, 300);
    }

    @Bean("discordExecutor")
    public Executor discordExecutor() {
        return buildExecutor("discord-async-", 1, 2, 50);
    }

    @Bean("s3DeleteExecutor")
    public Executor s3DeleteExecutor() {
        return buildExecutor("s3-delete-", 1, 2, 50);
    }

    private Executor buildExecutor(
            final String threadNamePrefix,
            final int corePoolSize,
            final int maxPoolSize,
            final int queueCapacity
    ) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setQueueCapacity(queueCapacity);
        executor.setThreadNamePrefix(threadNamePrefix);
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return (ex, method, params) -> {
            log.error("[ASYNC UNEXPECTED ERROR]: Method: {}, Exception: {}", method.getName(), ex.getMessage(), ex);
        };
    }
}
