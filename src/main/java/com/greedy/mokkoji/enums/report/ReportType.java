package com.greedy.mokkoji.enums.report;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ReportType {

    CLUB("동아리"),
    RECRUITMENT("모집글"),
    COMMENT("댓글"),
    RATING("평점");

    private final String description;
}
