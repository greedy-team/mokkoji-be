package com.greedy.mokkoji.enums.university;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum UniversityCode {
    SEJONG("세종대학교"),
    KONKUK("건국대학교"),
    HANYANG("한양대학교"),
    NO_CHOSEN("선택 안 함");

    private final String universityName;
}
