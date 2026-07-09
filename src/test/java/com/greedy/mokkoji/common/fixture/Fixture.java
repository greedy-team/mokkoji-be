package com.greedy.mokkoji.common.fixture;

import com.greedy.mokkoji.api.user.dto.resopnse.kakao.KakaoAccountResponse;
import com.greedy.mokkoji.api.user.dto.resopnse.kakao.KakaoProfileResponse;
import com.greedy.mokkoji.api.user.dto.resopnse.kakao.KakaoUserInfoResponse;
import com.greedy.mokkoji.db.admin.entity.Admin;
import com.greedy.mokkoji.db.club.entity.Club;
import com.greedy.mokkoji.db.favorite.entity.Favorite;
import com.greedy.mokkoji.db.recruitment.entity.Recruitment;
import com.greedy.mokkoji.db.university.entity.University;
import com.greedy.mokkoji.db.user.entity.User;
import com.greedy.mokkoji.enums.admin.AdminRole;
import com.greedy.mokkoji.enums.club.ClubAffiliation;
import com.greedy.mokkoji.enums.club.ClubCategory;
import com.greedy.mokkoji.enums.university.UniversityCode;
import com.greedy.mokkoji.enums.user.UserRole;

import java.time.LocalDateTime;
import java.util.List;

public class Fixture {

    public static final String FIXTURE_CLUB_LOGO = "그리디_로고";
    public static final List<String> FIXTURE_RECRUITMENT_IMAGE_NAME = List.of("모집글_이미지_이름.png");

    public static University createUniversity() {
        return University.builder()
                .name("세종대학교")
                .code(UniversityCode.SEJONG)
                .logo("세종대_로고")
                .build();
    }

    public static User createUser() {
        return User.builder()
                .code("Ab1234")
                .name("모꼬지")
                .kakaoId("kakao-12341234")
                .email("모꼬지@test.com")
                .isEmailOn(true)
                .role(UserRole.NORMAL)
                .build();
    }

    public static User createAnotherUser() {
        return User.builder()
                .code("cD5678")
                .name("다른사용자")
                .kakaoId("kakao-87654321")
                .email("another@test.com")
                .isEmailOn(true)
                .role(UserRole.NORMAL)
                .build();
    }

    public static User createUserWithRole(UserRole role) {
        return User.builder()
                .code("Ed9012")
                .name("모꼬지")
                .kakaoId("kakao-22222222")
                .email("모꼬지@test.com")
                .isEmailOn(true)
                .role(role)
                .build();
    }

    public static KakaoUserInfoResponse createKakaoUserInfoResponse(final String kakaoId, final String nickname) {
        return new KakaoUserInfoResponse(
                kakaoId,
                new KakaoAccountResponse(
                        new KakaoProfileResponse(nickname)
                )
        );
    }

    public static KakaoUserInfoResponse createKakaoUserInfoResponseWithoutNickname(final String kakaoId) {
        return new KakaoUserInfoResponse(
                kakaoId,
                new KakaoAccountResponse(null)
        );
    }

    public static Admin createAdmin() {
        return Admin.builder()
                .loginId("admin@konkuk.ac.kr")
                .password("encodedPassword")
                .role(AdminRole.MOKKOJI_ADMIN)
                .build();
    }

    public static Club createClub(University university) {
        return createClub(university, null);
    }

    public static Club createClub(University university, User master) {
        return Club.builder()
                .name("그리디")
                .university(university)
                .clubCategory(ClubCategory.ACADEMIC_CULTURAL)
                .clubAffiliation(ClubAffiliation.DEPARTMENT_CLUB)
                .logo(FIXTURE_CLUB_LOGO)
                .description("세종대 최고의 코딩 동아리")
                .instagram("www.그리디.com")
                .master(master)
                .build();
    }

    public static Club createAnotherClub(University university) {
        return Club.builder()
                .name("En#")
                .university(university)
                .clubCategory(ClubCategory.ACADEMIC_CULTURAL)
                .clubAffiliation(ClubAffiliation.DEPARTMENT_CLUB)
                .logo(FIXTURE_CLUB_LOGO)
                .description("커리큘럼이 체계적인 코딩 동아리")
                .instagram("www.En#.com")
                .build();
    }

    public static Club createClubWithCategoryAndAffiliation(University university, ClubCategory category, ClubAffiliation affiliation) {
        return Club.builder()
                .name("그리디")
                .university(university)
                .clubCategory(category)
                .clubAffiliation(affiliation)
                .logo(FIXTURE_CLUB_LOGO)
                .description("세종대 최고의 코딩 동아리")
                .instagram("www.그리디.com")
                .build();
    }

    public static Recruitment createRecruitment(Club club) {
        return Recruitment.builder()
                .club(club)
                .recruitStart(LocalDateTime.of(2025, 1, 1, 12, 0, 0))
                .recruitEnd(LocalDateTime.of(2025, 2, 2, 12, 0, 0))
                .title("모집글 제목")
                .content("그리디 모집글")
                .recruitForm("그리디 모집 링크")
                .isAlwaysRecruiting(false)
                .build();
    }

    public static Recruitment createOrderRecruitment(Club club) {
        return Recruitment.builder()
                .club(club)
                .recruitStart(LocalDateTime.of(2024, 1, 1, 12, 0, 0))
                .recruitEnd(LocalDateTime.of(2024, 2, 2, 12, 0, 0))
                .title("오래된 모집글 제목")
                .content("오래된 모집글")
                .recruitForm("오래된 모집 링크")
                .isAlwaysRecruiting(false)
                .build();
    }

    public static Recruitment createNewerRecruitment(Club club) {
        return Recruitment.builder()
                .club(club)
                .recruitStart(LocalDateTime.of(2025, 1, 1, 12, 0, 0))
                .recruitEnd(LocalDateTime.of(2025, 2, 2, 12, 0, 0))
                .title("최신 모집글 제목")
                .content("최신 모집글")
                .recruitForm("최신 모집 링크")
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

    public static Recruitment createRecruitmentWithTimes(
            Club club,
            LocalDateTime recruitStart,
            LocalDateTime recruitEnd,
            boolean isAlwaysRecruiting
    ) {
        return Recruitment.builder()
                .club(club)
                .recruitStart(recruitStart)
                .recruitEnd(recruitEnd)
                .title("8월 모집글")
                .content("그리디 모집글")
                .isAlwaysRecruiting(isAlwaysRecruiting)
                .build();
    }

    public static Favorite createFavorite(Club club, User user) {
        return Favorite.builder()
                .club(club)
                .user(user)
                .build();
    }
}
