package com.greedy.mokkoji.enums.clubmaster;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TransferStatus {
    PENDING("대기 중"),
    APPROVED("수락");

    private final String description;
}
