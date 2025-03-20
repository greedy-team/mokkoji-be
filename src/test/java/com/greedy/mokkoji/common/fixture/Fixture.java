package com.greedy.mokkoji.common.fixture;

import com.greedy.mokkoji.db.club.entity.Club;
import com.greedy.mokkoji.db.favorite.entity.Favorite;
import com.greedy.mokkoji.db.recruitment.entity.Recruitment;
import com.greedy.mokkoji.db.user.entity.User;
import com.greedy.mokkoji.enums.club.ClubAffiliation;
import com.greedy.mokkoji.enums.club.ClubCategory;

import java.time.LocalDateTime;

public class Fixture {
    public static final String FIXTURE_CLUB_LOGO = "그리디_로고";

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
                .logo(FIXTURE_CLUB_LOGO)
                .description("세종대 최고의 코딩 동아리")
                .instagram("www.그리디.com")
                .build();
    }

    public static Recruitment createRecruitment(Club club) {
        return Recruitment.builder()
                .club(club)
                .recruitStart(LocalDateTime.of(2025,01,01,12,00,00))
                .recruitEnd(LocalDateTime.of(2025,02,02,12,00,00))
                .content("그리디 모집글")
                .build();
    }

    public static Favorite createFavorite(Club club, User user) {
        return Favorite.builder()
                .club(club)
                .user(user)
                .build();
    }
}

