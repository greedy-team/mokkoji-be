package com.greedy.mokkoji.enums.application;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ApplicationStatus {

    PENDING("검토 중"),
    APPROVED("승인"),
    REJECTED("거절");

    private final String description;
}
