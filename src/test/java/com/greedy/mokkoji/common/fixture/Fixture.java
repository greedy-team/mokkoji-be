package com.greedy.mokkoji.common.fixture;

import com.greedy.mokkoji.db.club.entity.Club;
import com.greedy.mokkoji.db.user.entity.User;
import com.greedy.mokkoji.enums.club.ClubAffiliation;
import com.greedy.mokkoji.enums.club.ClubCategory;

public class Fixture {
    public static User createUser() {
        return User.builder()
                .name("모꼬지")
                .studentId("12341234")
                .grade("4")
                .department("컴퓨터공학과")
                .email("모꼬지@test.com")
                .build();
    }

    public static Club createClub() {
        return Club.builder()
                .name("그리디")
                .clubCategory(ClubCategory.ACADEMIC_CULTURAL)
                .clubAffiliation(ClubAffiliation.DEPARTMENT_CLUB)
                .logo("그리디 로고")
                .description("세종대 최고의 코딩 동아리")
                .instagram("www.그리디.com")
                .build();
    }
}
