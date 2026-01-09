package com.greedy.mokkoji.common.fixture;

import com.greedy.mokkoji.db.club.entity.Club;
import com.greedy.mokkoji.db.favorite.entity.Favorite;
import com.greedy.mokkoji.db.recruitment.entity.Recruitment;
import com.greedy.mokkoji.db.user.entity.User;
import com.greedy.mokkoji.enums.club.ClubAffiliation;
import com.greedy.mokkoji.enums.club.ClubCategory;

import com.greedy.mokkoji.enums.user.UserRole;
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
                .role(UserRole.GREEDY_ADMIN)
                .build();
    }

    public static User createAnotherUser() {
        return User.builder()
            .name("다른사용자")
            .studentId("87654321")
            .grade("3")
            .department("소프트웨어학과")
            .email("another@test.com")
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
                .clubMasterStudentId("12341234")
                .build();
    }

    public static Recruitment createRecruitment(Club club) {
        return Recruitment.builder()
            .club(club)
            .recruitStart(LocalDateTime.of(2025, 1, 1, 12, 0, 0))
            .recruitEnd(LocalDateTime.of(2025, 2, 2, 12, 0, 0))
            .title("모집글 제목")
            .content("그리디 모집글")
            .isAlwaysRecruiting(false)
            .build();
    }

    public static Recruitment createRecruitmentOfAugust(Club club) {
        return Recruitment.builder()
            .club(club)
            .recruitStart(LocalDateTime.of(2025, 8, 1, 12, 0, 0))
            .recruitEnd(LocalDateTime.of(2025, 9, 2, 12, 0, 0))
            .title("8월 모집글")
            .content("그리디 모집글")
            .isAlwaysRecruiting(false)
            .build();
    }

    public static Recruitment createLastedRecruitmentOfAugust(Club club) {
        return Recruitment.builder()
            .club(club)
            .recruitStart(LocalDateTime.of(2025, 8, 1, 12, 0, 0))
            .recruitEnd(LocalDateTime.of(2025, 9, 2, 12, 0, 0))
            .title("8월 최신 모집글")
            .content("그리디 모집글")
            .isAlwaysRecruiting(false)
            .build();
    }

    public static Favorite createFavorite(Club club, User user) {
        return Favorite.builder()
            .club(club)
            .user(user)
            .build();
    }
}
