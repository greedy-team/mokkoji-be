package com.greedy.mokkoji.enums;

<<<<<<<< HEAD:src/main/java/com/greedy/mokkoji/enums/RecruitStatus.java
public enum RecruitStatus {
    OPEN
========
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ClubAffiliation {

    CENTRAL_CLUB("중앙동아리"),
    DEPARTMENT_CLUB("가인준동아리");

    private final String description;
>>>>>>>> dae37fe (Feat: 동아리 카테고리, 소속, 모집 상태 enum 값 추가):src/main/java/com/greedy/mokkoji/enums/ClubAffiliation.java
}
