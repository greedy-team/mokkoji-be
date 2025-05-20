package com.greedy.mokkoji.enums.recruitment;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public enum RecruitStatus {
    IMMINENT(0),
    OPEN(1),
    BEFORE(2),
    CLOSED(3);

    private final int priority;

    RecruitStatus(int priority) {
        this.priority = priority;
    }

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


