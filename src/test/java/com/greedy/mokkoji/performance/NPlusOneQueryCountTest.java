package com.greedy.mokkoji.performance;

import com.greedy.mokkoji.api.club.service.ClubService;
import com.greedy.mokkoji.api.clubapplication.service.ClubApplicationService;
import com.greedy.mokkoji.api.clubmaster.service.ClubMasterService;
import com.greedy.mokkoji.api.comment.service.CommentService;
import com.greedy.mokkoji.api.external.AppDataS3Client;
import com.greedy.mokkoji.api.favorite.service.FavoriteService;
import com.greedy.mokkoji.api.recruitment.service.RecruitmentService;
import com.greedy.mokkoji.common.AbstractTest;
import com.greedy.mokkoji.common.fixture.Fixture;
import com.greedy.mokkoji.db.club.entity.Club;
import com.greedy.mokkoji.db.clubapplication.entity.ClubApplication;
import com.greedy.mokkoji.db.clubapplication.repository.ClubApplicationRepository;
import com.greedy.mokkoji.db.clubmaster.entity.ClubMasterApplication;
import com.greedy.mokkoji.db.clubmaster.repository.ClubMasterApplicationRepository;
import com.greedy.mokkoji.db.comment.entity.Comment;
import com.greedy.mokkoji.db.university.entity.University;
import com.greedy.mokkoji.db.user.entity.User;
import com.greedy.mokkoji.enums.club.ClubAffiliation;
import com.greedy.mokkoji.enums.club.ClubCategory;
import com.greedy.mokkoji.enums.user.UserRole;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.hibernate.Session;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.function.Supplier;

/**
 * 각 목록 조회 API가 "1번의 요청에 실제로 몇 개의 쿼리를 날리는지"를 Hibernate Statistics로 실측한다.
 *
 * 측정 원칙 (옵시디언 실측 노트 방법론):
 *  - 시드 데이터를 별도 트랜잭션으로 "커밋"한 뒤 서비스를 호출해야 N+1이 진짜로 재현된다.
 *    (setUp과 요청이 같은 1차 캐시를 공유하면 지연 로딩이 캐시로 해결돼 N+1이 안 잡히는 함정)
 *  - assert로 "N보다 작다" 식이 아니라, 실제 쿼리 수를 로그로 직접 확인한다.
 *  - 데이터 개수(N)를 늘려가며 쿼리 수가 N에 비례해 증가하면 N+1 확정.
 */
@SpringBootTest
@DisplayName("N+1 쿼리 수 실측 - 목록 조회 API 6종")
class NPlusOneQueryCountTest extends AbstractTest {

    private static final int N = 5; // 목록 항목 수 (클럽/모집/신청 등)

    @PersistenceContext
    private EntityManager em;

    @Autowired private TransactionTemplate txTemplate;

    @Autowired private ClubService clubService;
    @Autowired private FavoriteService favoriteService;
    @Autowired private RecruitmentService recruitmentService;
    @Autowired private CommentService commentService;
    @Autowired private ClubMasterService clubMasterService;
    @Autowired private ClubApplicationService clubApplicationService;

    @Autowired private ClubMasterApplicationRepository clubMasterApplicationRepository;
    @Autowired private ClubApplicationRepository clubApplicationRepository;

    // 실제 AWS 호출 방지 - getPublicUrl은 목록 매핑에서 항목마다 불리지만 DB 쿼리는 아님
    @MockitoBean private AppDataS3Client appDataS3Client;

    // AbstractTest.cleanUpDatabase()는 application 계열 테이블을 안 지운다.
    // 이 테이블이 user를 FK로 참조하므로, 다음 테스트의 부모 cleanup이 user 삭제에 실패한다.
    // 각 테스트가 끝날 때 자기가 만든 application 데이터를 직접 비워 뒷 테스트를 보호한다.
    @AfterEach
    void cleanApplicationTables() {
        clubMasterApplicationRepository.deleteAll();
        clubApplicationRepository.deleteAll();
    }

    private Statistics statistics() {
        return em.unwrap(Session.class).getSessionFactory().getStatistics();
    }

    /**
     * 시드 데이터를 "별도 트랜잭션으로 커밋"한다. 이렇게 해야 뒤이은 서비스 호출이
     * 새 트랜잭션·새 영속성 컨텍스트(1차 캐시 없음)에서 시작돼 N+1이 진짜로 재현된다.
     */
    private <T> T seed(Supplier<T> work) {
        return txTemplate.execute(status -> work.get());
    }

    private void resetStats() {
        Statistics s = statistics();
        s.setStatisticsEnabled(true);
        s.clear();
    }

    private long queryCount() {
        return statistics().getPrepareStatementCount();
    }

    private void report(String api, int n, long count) {
        // 이모지는 XML 리포트 CDATA를 깨뜨리므로 결과 로그에는 넣지 않는다.
        System.out.printf("%n[NPLUS1] %-38s | items=%d | queries=%d%n", api, n, count);
    }

    private void stubS3() {
        org.mockito.BDDMockito.given(appDataS3Client.getPublicUrl(org.mockito.ArgumentMatchers.anyString()))
                .willReturn("https://cdn.test/logo.png");
    }

    private User newUser(University university, int i) {
        return User.builder()
                .university(university)
                .code("code-" + i)
                .name("유저" + i)
                .kakaoId("kakao-" + i)
                .email("user" + i + "@test.com")
                .isEmailOn(true)
                .role(UserRole.NORMAL)
                .build();
    }

    // =========================================================
    // 🔴1  GET /clubs/search  ->  ClubService.findClubsByConditions
    // =========================================================
    @Test
    @DisplayName("🔴1 /clubs/search (findClubsByConditions) - 로그인 유저")
    void clubsSearch() {
        stubS3();
        Long userId = seed(() -> {
            University university = universityRepository.save(Fixture.createUniversity());
            User user = userRepository.save(newUser(university, 0));
            for (int i = 0; i < N; i++) {
                Club club = clubRepository.save(Fixture.createClubWithCategoryAndAffiliation(
                        university, ClubCategory.ACADEMIC_CULTURAL, ClubAffiliation.DEPARTMENT_CLUB));
                recruitmentRepository.save(Fixture.createRecruitment(club));
                favoriteRepository.save(Fixture.createFavorite(club, user));
            }
            return user.getId();
        });

        Pageable pageable = PageRequest.of(0, N);
        resetStats();
        clubService.findClubsByConditions(userId, null, null, null, null, null, pageable);
        report("1 clubs-search", N, queryCount());
    }

    // =========================================================
    // 🔴2  즐겨찾기 목록  ->  FavoriteService.findFavoriteClubs
    // =========================================================
    @Test
    @DisplayName("🔴2 즐겨찾기 목록 (findFavoriteClubs)")
    void favoriteClubs() {
        stubS3();
        Long userId = seed(() -> {
            University university = universityRepository.save(Fixture.createUniversity());
            User user = userRepository.save(newUser(university, 0));
            for (int i = 0; i < N; i++) {
                Club club = clubRepository.save(Fixture.createClubWithCategoryAndAffiliation(
                        university, ClubCategory.ACADEMIC_CULTURAL, ClubAffiliation.DEPARTMENT_CLUB));
                recruitmentRepository.save(Fixture.createRecruitment(club));
                favoriteRepository.save(Fixture.createFavorite(club, user));
            }
            return user.getId();
        });

        Pageable pageable = PageRequest.of(0, N);
        resetStats();
        favoriteService.findFavoriteClubs(userId, pageable);
        report("2 favorite-clubs", N, queryCount());
    }

    // =========================================================
    // 🟠4  내 동아리장 신청 목록  ->  ClubMasterService.getMyClubMasterApplications
    // =========================================================
    @Test
    @DisplayName("🟠4 내 동아리장 신청 목록 (getMyClubMasterApplications)")
    void myClubMasterApplications() {
        Long userId = seed(() -> {
            University university = universityRepository.save(Fixture.createUniversity());
            User user = userRepository.save(newUser(university, 0));
            for (int i = 0; i < N; i++) {
                Club club = clubRepository.save(Fixture.createClubWithCategoryAndAffiliation(
                        university, ClubCategory.ACADEMIC_CULTURAL, ClubAffiliation.DEPARTMENT_CLUB));
                clubMasterApplicationRepository.save(ClubMasterApplication.builder()
                        .university(university)
                        .club(club)
                        .user(user)
                        .userName(user.getName())
                        .build());
            }
            return user.getId();
        });

        resetStats();
        clubMasterService.getMyClubMasterApplications(userId);
        report("4 my-clubmaster-apps", N, queryCount());
    }

    // =========================================================
    // 🟡5  GET /comments/{clubId}  ->  CommentService.getComments
    // =========================================================
    @Test
    @DisplayName("🟡5 /comments/{clubId} (getComments)")
    void comments() {
        Long clubId = seed(() -> {
            University university = universityRepository.save(Fixture.createUniversity());
            Club club = clubRepository.save(Fixture.createClub(university));
            for (int i = 0; i < N; i++) {
                User writer = userRepository.save(newUser(university, i));
                commentRepository.save(Comment.builder()
                        .rate(5.0)
                        .content("댓글" + i)
                        .club(club)
                        .user(writer)
                        .build());
            }
            return club.getId();
        });

        resetStats();
        commentService.getComments(null, clubId);
        report("5 comments", N, queryCount());
    }

    // =========================================================
    // 🟡6  내 동아리 개설 신청 목록  ->  ClubApplicationService.getMyClubApplications
    // =========================================================
    @Test
    @DisplayName("🟡6 내 개설 신청 목록 (getMyClubApplications)")
    void myClubApplications() {
        Long userId = seed(() -> {
            University university = universityRepository.save(Fixture.createUniversity());
            User user = userRepository.save(newUser(university, 0));
            for (int i = 0; i < N; i++) {
                clubApplicationRepository.save(ClubApplication.builder()
                        .university(university)
                        .applicant(user)
                        .applicantName(user.getName())
                        .clubName("신청동아리" + i)
                        .clubCategory(ClubCategory.ACADEMIC_CULTURAL)
                        .clubAffiliation(ClubAffiliation.DEPARTMENT_CLUB)
                        .logo("logo")
                        .instagram("insta")
                        .description("설명")
                        .build());
            }
            return user.getId();
        });

        resetStats();
        clubApplicationService.getMyClubApplications(userId);
        report("6 my-club-apps", N, queryCount());
    }
}
