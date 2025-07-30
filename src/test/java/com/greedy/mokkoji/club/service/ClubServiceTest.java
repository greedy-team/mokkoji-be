package com.greedy.mokkoji.club.service;

import com.greedy.mokkoji.api.club.dto.club.response.ClubDetailResponse;
import com.greedy.mokkoji.api.club.dto.club.response.ClubSearchResponse;
import com.greedy.mokkoji.api.club.service.ClubService;
import com.greedy.mokkoji.api.external.AppDataS3Client;
import com.greedy.mokkoji.db.club.entity.Club;
import com.greedy.mokkoji.db.club.repository.ClubRepository;
import com.greedy.mokkoji.db.favorite.repository.FavoriteRepository;
import com.greedy.mokkoji.db.recruitment.entity.Recruitment;
import com.greedy.mokkoji.db.recruitment.repository.RecruitmentRepository;
import com.greedy.mokkoji.enums.club.ClubAffiliation;
import com.greedy.mokkoji.enums.club.ClubCategory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("동아리 검색 및 필터링 서비스 테스트")
@ExtendWith(MockitoExtension.class)
class ClubServiceTest {

    @InjectMocks
    private ClubService clubService;

    @Mock
    private ClubRepository clubRepository;

    @Mock
    private RecruitmentRepository recruitmentRepository;

    @Mock
    private FavoriteRepository favoriteRepository;

    @Mock
    private AppDataS3Client appDataS3Client;

    private Club club1;
    private Club club2;
    private Recruitment recruitment1;
    private Recruitment recruitment2;

    @BeforeEach
    void setUp() {
        club1 = Club.builder()
                .name("testClub1")
                .clubCategory(ClubCategory.ACADEMIC_CULTURAL)
                .clubAffiliation(ClubAffiliation.CENTRAL_CLUB)
                .description("testDescription1")
                .logo("testLogo1")
                .instagram("testInstagramURL1")
                .build();
        ReflectionTestUtils.setField(club1, "id", 1L);

        recruitment1 = Recruitment.builder()
                .club(club1)
                .content("testContent1")
                .recruitStart(LocalDateTime.of(2025, 1, 1, 12, 0))
                .recruitEnd(LocalDateTime.of(2025, 3, 30, 12, 0))
                .build();

        club2 = Club.builder()
                .name("testClub2")
                .clubCategory(ClubCategory.CULTURAL_ART)
                .clubAffiliation(ClubAffiliation.DEPARTMENT_CLUB)
                .description("testDescription2")
                .logo("testLogo2")
                .instagram("testInstagramURL2")
                .build();
        ReflectionTestUtils.setField(club2, "id", 2L);

        recruitment2 = Recruitment.builder()
                .club(club2)
                .content("testContent2")
                .recruitStart(LocalDateTime.of(2025, 1, 1, 12, 0))
                .recruitEnd(LocalDateTime.of(2025, 1, 30, 12, 0))
                .build();
    }

    @Test
    @DisplayName("전체 동아리 정보를 조회한다.")
    void findClubsByNoConditions() {
        //given
        final Long userId = 1L;
        final Long clubId1 = club1.getId();
        final Long clubId2 = club2.getId();
        final Pageable pageable = PageRequest.of(0, 10);
        final List<Club> clubs = List.of(club1, club2);
        final Page<Club> clubPage = new PageImpl<>(clubs, pageable, clubs.size());

        BDDMockito.given(clubRepository.findClubs(any(), any(), any(), any(), any())).willReturn(clubPage);
        BDDMockito.given(recruitmentRepository.findByClubId(clubId1)).willReturn(recruitment1);
        BDDMockito.given(recruitmentRepository.findByClubId(clubId2)).willReturn(recruitment2);
        BDDMockito.given(favoriteRepository.existsByUserIdAndClubId(userId, clubId1)).willReturn(true);
        BDDMockito.given(favoriteRepository.existsByUserIdAndClubId(userId, clubId2)).willReturn(false);
        BDDMockito.given(appDataS3Client.getPresignedUrl(club1.getLogo())).willReturn("testLogo1");
        BDDMockito.given(appDataS3Client.getPresignedUrl(club2.getLogo())).willReturn("testLogo2");

        //when
        final ClubSearchResponse response = clubService.findClubsByConditions(userId, null, null, null, null, pageable);

        //then
        assertThat(response.clubs()).hasSize(2);

        assertThat(response.clubs().get(0).name()).isEqualTo("testClub1");
        assertThat(response.clubs().get(0).category()).isEqualTo("학술");
        assertThat(response.clubs().get(0).affiliation()).isEqualTo("중앙동아리");
        assertThat(response.clubs().get(0).description()).isEqualTo("testDescription1");
        assertThat(response.clubs().get(0).recruitStartDate()).isEqualTo("2025-01-01");
        assertThat(response.clubs().get(0).recruitEndDate()).isEqualTo("2025-03-30");
        assertThat(response.clubs().get(0).imageURL()).isEqualTo("testLogo1");
        assertThat(response.clubs().get(0).isFavorite()).isEqualTo(true);

        assertThat(response.clubs().get(1).name()).isEqualTo("testClub2");
        assertThat(response.clubs().get(1).category()).isEqualTo("공연");
        assertThat(response.clubs().get(1).affiliation()).isEqualTo("기타동아리");
        assertThat(response.clubs().get(1).description()).isEqualTo("testDescription2");
        assertThat(response.clubs().get(1).recruitStartDate()).isEqualTo("2025-01-01");
        assertThat(response.clubs().get(1).recruitEndDate()).isEqualTo("2025-01-30");
        assertThat(response.clubs().get(1).imageURL()).isEqualTo("testLogo2");
        assertThat(response.clubs().get(1).isFavorite()).isEqualTo(false);

        assertThat(response.pagination().totalElements()).isEqualTo(2);

        BDDMockito.verify(clubRepository, times(1)).findClubs(any(), any(), any(), any(), any(Pageable.class));
        BDDMockito.verify(recruitmentRepository, times(2)).findByClubId(anyLong());
        BDDMockito.verify(favoriteRepository, times(2)).existsByUserIdAndClubId(anyLong(), anyLong());
    }

    @Test
    @DisplayName("동아리 상세 정보를 조회한다.")
    void findClubDetailInformation() {
        //given
        final Long userId = 1L;
        final Long clubId = club1.getId();

        BDDMockito.given(clubRepository.findById(clubId)).willReturn(Optional.ofNullable(club1));
        BDDMockito.given(recruitmentRepository.findByClubId(clubId)).willReturn(recruitment1);
        BDDMockito.given(favoriteRepository.existsByUserIdAndClubId(userId, clubId)).willReturn(true);
        BDDMockito.given(appDataS3Client.getPresignedUrl(club1.getLogo())).willReturn("testLogo1");

        //when
        ClubDetailResponse response = clubService.findClub(userId, clubId);

        //then
        assertThat(response).isNotNull();
        assertThat(response.name()).isEqualTo("testClub1");
        assertThat(response.category()).isEqualTo("학술");
        assertThat(response.affiliation()).isEqualTo("중앙동아리");
        assertThat(response.description()).isEqualTo("testDescription1");
        assertThat(response.recruitStartDate()).isEqualTo("2025-01-01");
        assertThat(response.recruitEndDate()).isEqualTo("2025-03-30");
        assertThat(response.imageURL()).isEqualTo("testLogo1");
        assertThat(response.isFavorite()).isEqualTo(true);
        assertThat(response.instagramLink()).isEqualTo("testInstagramURL1");
        assertThat(response.recruitPost()).isEqualTo("testContent1");

        verify(clubRepository, times(1)).findById(anyLong());
        verify(recruitmentRepository, times(1)).findByClubId(anyLong());
        verify(favoriteRepository, times(1)).existsByUserIdAndClubId(anyLong(), anyLong());

    }
}
