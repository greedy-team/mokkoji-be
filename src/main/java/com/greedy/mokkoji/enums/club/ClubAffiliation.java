package com.greedy.mokkoji.enums.club;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ClubAffiliation {

    CENTRAL_CLUB("중앙"),
    DEPARTMENT_CLUB("정인준/가인준"),
    SMALL_GROUP("소모임");

    private final String description;
}
