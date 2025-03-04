package com.greedy.mokkoji.enums.club;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ClubAffiliation {

    CENTRAL_CLUB("중앙동아리"),
    DEPARTMENT_CLUB("기타동아리");

    private final String description;
}
