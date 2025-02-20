package com.greedy.mokkoji.club;

import com.greedy.mokkoji.api.club.dto.club.ClubSearchCond;
import com.greedy.mokkoji.api.club.dto.club.ClubSearchResponse;
import com.greedy.mokkoji.api.club.service.ClubService;
import com.greedy.mokkoji.db.club.entity.Club;
import com.greedy.mokkoji.db.club.repository.ClubRepository;
import com.greedy.mokkoji.db.favorite.repository.FavoriteRepository;
import com.greedy.mokkoji.db.recruitment.entity.Recruitment;
import com.greedy.mokkoji.db.recruitment.repository.RecruitmentRepository;
import com.greedy.mokkoji.enums.ClubAffiliation;
import com.greedy.mokkoji.enums.ClubCategory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("동아리 검색 및 필터링 서비스 테스트")
@ExtendWith(MockitoExtension.class)
class ClubSearchTest {

    @InjectMocks
    private ClubService clubService;

    @Mock
    private ClubRepository clubRepository;

    @Mock
    private RecruitmentRepository recruitmentRepository;

    @Mock
    private FavoriteRepository favoriteRepository;

    private Club club;
    private Recruitment recruitment;

    @BeforeEach
    void setUp() {
        club = Club.builder()
                .name("testClub")
                .clubCategory(ClubCategory.ACADEMIC_CULTURAL)
                .clubAffiliation(ClubAffiliation.CENTRAL_CLUB)
                .description("testDescription")
                .logo("testLogo")
                .instagram("testInstagramURL")
                .build();
        ReflectionTestUtils.setField(club, "id", 1L);

        recruitment = Recruitment.builder()
                .club(club)
                .content("testContent")
                .recruitStart(LocalDateTime.of(2025, 1, 1, 12, 0))
                .recruitEnd(LocalDateTime.of(2025, 2, 20, 12, 0))
                .build();
    }

    @Test
    @DisplayName("조건에 맞는 동아리를 검색할 수 있다.")
    void findClubsByConditions() {
        final Long userId = 1L;
        final Long clubId = club.getId();
        final ClubSearchCond cond = ClubSearchCond.builder().build();
        final Pageable pageable = PageRequest.of(0, 10);
        final List<Club> clubs = List.of(club);
        final Page<Club> clubPage = new PageImpl<>(clubs, pageable, clubs.size());

        BDDMockito.given(clubRepository.findClubs(cond, pageable)).willReturn(clubPage);
        BDDMockito.given(recruitmentRepository.findByClubId(clubId)).willReturn(recruitment);
        BDDMockito.given(favoriteRepository.existsByUserIdAndClubId(userId, clubId)).willReturn(true);

        final ClubSearchResponse response = clubService.findClubsByConditions(userId, cond, pageable);

        assertThat(response.clubs()).hasSize(1);
        assertThat(response.clubs().get(0).name()).isEqualTo("testClub");
        assertThat(response.clubs().get(0).category()).isEqualTo("학술/교양");
        assertThat(response.clubs().get(0).affiliation()).isEqualTo("중앙동아리");
        assertThat(response.clubs().get(0).description()).isEqualTo("testDescription");
        assertThat(response.clubs().get(0).recruitStartDate()).isEqualTo("2025-01-01");
        assertThat(response.clubs().get(0).recruitEndDate()).isEqualTo("2025-02-20");
        assertThat(response.clubs().get(0).imageURL()).isEqualTo("testLogo");
        assertThat(response.clubs().get(0).isFavorite()).isEqualTo(true);
        assertThat(response.pagination().totalElements()).isEqualTo(1);

        BDDMockito.verify(clubRepository, times(1)).findClubs(any(ClubSearchCond.class), any(Pageable.class));
        BDDMockito.verify(recruitmentRepository, times(1)).findByClubId(anyLong());
        BDDMockito.verify(favoriteRepository, times(1)).existsByUserIdAndClubId(anyLong(), anyLong());
    }
}
