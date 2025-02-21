package com.greedy.mokkoji.enums.club;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ClubAffiliation {

    CENTRAL_CLUB("중앙동아리"),
    DEPARTMENT_CLUB("가인준동아리");

    private final String description;
}
