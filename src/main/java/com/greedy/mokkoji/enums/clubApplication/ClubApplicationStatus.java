package com.greedy.mokkoji.enums.clubApplication;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ClubApplicationStatus {

    PENDING("검토 중"),
    APPROVED("승인"),
    REJECTED("거절");

    private final String description;
}
