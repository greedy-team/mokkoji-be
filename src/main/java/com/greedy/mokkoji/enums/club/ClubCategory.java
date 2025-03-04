package com.greedy.mokkoji.enums.club;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ClubCategory {

    CULTURAL_ART("공연"),
    ACADEMIC_CULTURAL("학술"),
    VOLUNTEER_SOCIAL("봉사"),
    SOCIAL("문화"),
    SPORTS("체육"),
    RELIGIOUS("종교"),
    OTHER("기타");

    private final String description;
}
