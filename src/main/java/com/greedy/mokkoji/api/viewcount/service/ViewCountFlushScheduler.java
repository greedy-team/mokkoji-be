package com.greedy.mokkoji.api.viewcount.service;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(
        prefix = "view-count",
        name = "flush-enabled",
        havingValue = "true",
        matchIfMissing = true
)
public class ViewCountFlushScheduler {

    private final ViewCountFlushWorker viewCountFlushWorker;

    @Scheduled(
            scheduler = "viewCountScheduler",
            fixedDelayString = "${view-count.flush-interval:10s}",
            initialDelayString = "${view-count.flush-interval:10s}"
    )
    public void flushViewCounts() {
        viewCountFlushWorker.flushViewCounts();
    }
}
