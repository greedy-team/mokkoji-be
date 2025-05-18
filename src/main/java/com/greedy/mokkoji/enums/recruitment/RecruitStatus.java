package com.greedy.mokkoji.enums.recruitment;

import java.time.LocalDateTime;

public enum RecruitStatus {
    BEFORE,
    OPEN,
    IMMINENT,
    CLOSED;

    public static RecruitStatus from(LocalDateTime start, LocalDateTime end) {
        LocalDateTime now = LocalDateTime.now();

        if (now.isBefore(start)) {
            return BEFORE;
        } else if (now.isAfter(end)) {
            return CLOSED;
        } else if (now.isAfter(end.minusDays(7))) {
            return IMMINENT;
        } else {
            return OPEN;
        }
    }
}

