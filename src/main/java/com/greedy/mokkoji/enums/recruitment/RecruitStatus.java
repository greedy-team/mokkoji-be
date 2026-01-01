package com.greedy.mokkoji.enums.recruitment;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public enum RecruitStatus {
    ALWAYS(0),
    IMMINENT(1),
    OPEN(2),
    BEFORE(3),
    CLOSED(4);

    private final int priority;

    public static RecruitStatus from(boolean isAlwaysRecruiting, LocalDateTime recruitStart, LocalDateTime recruitEnd) {
        if (isAlwaysRecruiting) {
            return ALWAYS;
        }

        LocalDateTime now = LocalDateTime.now();

        if (now.isBefore(recruitStart)) {
            return BEFORE;
        }
        if (now.isAfter(recruitEnd)) {
            return CLOSED;
        }
        if (now.isAfter(recruitEnd.minusDays(7))) {
            return IMMINENT;
        }
        return OPEN;
    }

    public static RecruitStatus from(LocalDateTime start, LocalDateTime end) {
        return from(false, start, end);
    }
}


