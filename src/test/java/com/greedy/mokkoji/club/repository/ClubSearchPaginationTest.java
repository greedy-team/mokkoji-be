package com.greedy.mokkoji.club.repository;

import com.greedy.mokkoji.common.AbstractTest;
import com.greedy.mokkoji.common.fixture.Fixture;
import com.greedy.mokkoji.db.club.entity.Club;
import com.greedy.mokkoji.db.recruitment.entity.Recruitment;
import com.greedy.mokkoji.db.university.entity.University;
import com.greedy.mokkoji.enums.club.ClubAffiliation;
import com.greedy.mokkoji.enums.club.ClubCategory;
import com.greedy.mokkoji.enums.university.UniversityCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@DisplayName("clubs/search 페이지네이션 누락 (#508)")
class ClubSearchPaginationTest extends AbstractTest {

    private University seedUniversity() {
        return universityRepository.save(Fixture.createUniversity());
    }

    private Club seedClub(final University university, final String name) {
        return clubRepository.save(Club.builder()
                .name(name)
                .university(university)
                .clubCategory(ClubCategory.ACADEMIC_CULTURAL)
                .clubAffiliation(ClubAffiliation.DEPARTMENT_CLUB)
                .logo(Fixture.FIXTURE_CLUB_LOGO)
                .description("동아리 설명")
                .instagram("www.test.com")
                .build());
    }

    private void seedRecruitments(final Club club, final int count) {
        for (int i = 0; i < count; i++) {
            recruitmentRepository.save(Recruitment.builder()
                    .club(club)
                    .recruitStart(LocalDateTime.now().minusDays(10))
                    .recruitEnd(LocalDateTime.now().plusDays(10))
                    .title("모집글 제목 " + i)
                    .content("모집글 본문 " + i)
                    .recruitForm("모집 링크")
                    .isAlwaysRecruiting(false)
                    .build());
        }
    }

    @Test
    @DisplayName("모집글이 여러 개인 동아리가 있어도 전체 동아리가 누락 없이 조회된다")
    void doesNotDropClubsWhenSomeHaveManyRecruitments() {
        final University university = seedUniversity();

        final int totalClubs = 10;
        for (int i = 0; i < totalClubs; i++) {
            final Club club = seedClub(university, "동아리" + i);
            seedRecruitments(club, i < 5 ? 5 : 1);
        }

        final Pageable pageable = PageRequest.of(0, totalClubs);
        final Page<Club> page = clubRepository.findClubsWithLatestRecruitment(
                UniversityCode.SEJONG, null, null, null, null, pageable);

        final List<Club> clubs = page.getContent();

        assertThat(clubs)
                .as("모집글 개수와 무관하게 동아리 %d개가 모두 반환되어야 한다", totalClubs)
                .hasSize(totalClubs);
    }

    @Test
    @DisplayName("totalElements가 조인으로 뻥튀기되지 않는다")
    void totalElementsIsNotInflatedByJoin() {
        final University university = seedUniversity();

        final int totalClubs = 3;
        for (int i = 0; i < totalClubs; i++) {
            seedRecruitments(seedClub(university, "동아리" + i), 4);
        }

        final Page<Club> page = clubRepository.findClubsWithLatestRecruitment(
                UniversityCode.SEJONG, null, null, null, null, PageRequest.of(0, 10));

        assertThat(page.getTotalElements())
                .as("모집글 수가 아니라 동아리 수를 세야 한다")
                .isEqualTo(totalClubs);
    }

    @Test
    @DisplayName("모집글이 하나도 없는 동아리도 조회에서 빠지지 않는다")
    void includesClubsWithoutAnyRecruitment() {
        final University university = seedUniversity();

        seedClub(university, "모집글없는동아리");
        seedRecruitments(seedClub(university, "모집글있는동아리"), 2);

        final Page<Club> page = clubRepository.findClubsWithLatestRecruitment(
                UniversityCode.SEJONG, null, null, null, null, PageRequest.of(0, 10));

        assertThat(page.getContent())
                .as("모집글 유무는 조회 대상 여부를 바꾸지 않는다")
                .extracting(Club::getName)
                .containsExactlyInAnyOrder("모집글없는동아리", "모집글있는동아리");
    }

    @Test
    @DisplayName("모집글이 없어도 동아리 이름으로 검색된다")
    void findsClubByNameEvenWithoutAnyRecruitment() {
        final University university = seedUniversity();
        seedClub(university, "모집글없는검색대상");

        final Page<Club> page = clubRepository.findClubsWithLatestRecruitment(
                UniversityCode.SEJONG, "검색대상", null, null, null, PageRequest.of(0, 10));

        assertThat(page.getContent())
                .as("이름 검색은 모집글 존재 여부와 무관해야 한다")
                .extracting(Club::getName)
                .containsExactly("모집글없는검색대상");
    }

    @Test
    @DisplayName("키워드가 과거 모집글 본문에만 있어도 해당 동아리가 검색된다")
    void findsClubWhenKeywordExistsOnlyInOlderRecruitmentContent() {
        final University university = seedUniversity();
        final Club club = seedClub(university, "검색대상동아리");

        recruitmentRepository.save(Recruitment.builder()
                .club(club)
                .recruitStart(LocalDateTime.now().minusDays(100))
                .recruitEnd(LocalDateTime.now().minusDays(90))
                .title("옛날 모집글")
                .content("여기에만 특별키워드 가 있다")
                .recruitForm("링크")
                .isAlwaysRecruiting(false)
                .build());
        recruitmentRepository.save(Recruitment.builder()
                .club(club)
                .recruitStart(LocalDateTime.now().minusDays(5))
                .recruitEnd(LocalDateTime.now().plusDays(5))
                .title("최신 모집글")
                .content("최신 본문에는 없음")
                .recruitForm("링크")
                .isAlwaysRecruiting(false)
                .build());

        final Page<Club> page = clubRepository.findClubsWithLatestRecruitment(
                UniversityCode.SEJONG, "특별키워드", null, null, null, PageRequest.of(0, 10));

        assertThat(page.getContent())
                .as("검색은 최신 1건이 아니라 모든 모집글을 대상으로 해야 한다")
                .extracting(Club::getName)
                .containsExactly("검색대상동아리");
    }
}
