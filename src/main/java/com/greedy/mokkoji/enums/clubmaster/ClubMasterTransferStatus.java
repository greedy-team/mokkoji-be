package com.greedy.mokkoji.enums.clubmaster;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ClubMasterTransferStatus {
    PENDING("승인 전"),
    APPROVED("승인"),
    EXPIRED("링크 만료");

    private final String description;
}
