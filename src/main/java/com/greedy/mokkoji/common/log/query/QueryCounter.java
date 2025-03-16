package com.greedy.mokkoji.common.log.query;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

@Getter
@Component
@RequestScope
@RequiredArgsConstructor
public class QueryCounter {
    private final Long time = System.currentTimeMillis();
    private Long count = 0L;

    public void increaseCount() {
        count++;
    }
}
