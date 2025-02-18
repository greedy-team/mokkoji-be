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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.time.LocalDateTime;
import java.util.List;

import static com.greedy.mokkoji.db.club.entity.Club.builder;
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

    @Test
    @DisplayName("조건에 맞는 동아리를 검색할 수 있다.")
    void findClubsByConditions() {
        Long userId = 1L;
        ClubSearchCond cond = ClubSearchCond.builder().build();
        Pageable pageable = PageRequest.of(0, 10);

        Club club = builder()
                .name("testClub")
                .clubCategory(ClubCategory.ACADEMIC_CULTURAL)
                .clubAffiliation(ClubAffiliation.CENTRAL_CLUB)
                .description("testDescription")
                .logo("testLogo")
                .instagram("testInstagramURL")
                .build();

        Recruitment recruitment = Recruitment.builder()
                .club(club)
                .content("testContent")
                .recruitStart(LocalDateTime.of(25, 1, 1, 12, 0))
                .recruitEnd(LocalDateTime.of(25, 2, 20, 12, 0))
                .build();

        Page<Club> clubPage = new PageImpl<>(List.of(club), pageable, 1);

        BDDMockito.given(clubRepository.findClubs(any(ClubSearchCond.class), any(Pageable.class)))
                .willReturn(clubPage);
        BDDMockito.given(favoriteRepository.findClubIdByUserId(userId))
                .willReturn(List.of(1L));
        BDDMockito.given(recruitmentRepository.findByClub(any(Club.class))).willReturn(recruitment)
                .willReturn(recruitment);

        ClubSearchResponse response = clubService.findClubsByConditions(userId, cond, pageable);

        assertThat(response.clubs()).isNotEmpty();
        assertThat(response.clubs().get(0).name()).isEqualTo("testClub");
        assertThat(response.pagination().totalElements()).isEqualTo(1);

        verify(clubRepository, times(1)).findClubs(any(ClubSearchCond.class), any(Pageable.class));
        verify(favoriteRepository, times(1)).findClubIdByUserId(userId);
        verify(recruitmentRepository, times(1)).findByClub(any(Club.class));
    }
}
