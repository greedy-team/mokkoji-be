package com.greedy.mokkoji.favorite;

import com.greedy.mokkoji.api.club.dto.club.ClubResponse;
import com.greedy.mokkoji.api.external.AppDataS3Client;
import com.greedy.mokkoji.api.favorite.service.FavoriteService;
import com.greedy.mokkoji.common.exception.MokkojiException;
import com.greedy.mokkoji.db.club.entity.Club;
import com.greedy.mokkoji.db.club.repository.ClubRepository;
import com.greedy.mokkoji.db.favorite.entity.Favorite;
import com.greedy.mokkoji.db.favorite.repository.FavoriteRepository;
import com.greedy.mokkoji.db.recruitment.entity.Recruitment;
import com.greedy.mokkoji.db.recruitment.repository.RecruitmentRepository;
import com.greedy.mokkoji.db.user.entity.User;
import com.greedy.mokkoji.db.user.repository.UserRepository;
import com.greedy.mokkoji.enums.club.ClubAffiliation;
import com.greedy.mokkoji.enums.club.ClubCategory;
import com.greedy.mokkoji.enums.message.FailMessage;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
@DisplayName("즐겨찾기 서비스 테스트")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
public class FavoriteTest {

    @InjectMocks
    FavoriteService favoriteService;

    @Mock
    UserRepository userRepository;

    @Mock
    ClubRepository clubRepository;

    @Mock
    FavoriteRepository favoriteRepository;

    @Mock
    RecruitmentRepository recruitmentRepository;

    @Mock
    private AppDataS3Client appDataS3Client;

    @Test
    @DisplayName("동아리를 즐겨찾기할 수 있다.")
    void addFavorite() {
        // given
        final User user = User.builder()
                .name("사용자 이름")
                .email("사용자 이메일")
                .grade("4")
                .department("사용자 학과")
                .studentId("사용자 학번")
                .build();
        ReflectionTestUtils.setField(user, "id", 1L);

        final Club club = Club.builder()
                .name("동아리 이름")
                .clubAffiliation(ClubAffiliation.CENTRAL_CLUB)
                .clubCategory(ClubCategory.CULTURAL_ART)
                .logo("동아리 로고")
                .description("동아리 설명")
                .instagram("동아리 인스타그램 링크")
                .build();
        ReflectionTestUtils.setField(club, "id", 1L);

        BDDMockito.given(userRepository.findById(any())).willReturn(Optional.ofNullable(user));
        BDDMockito.given(clubRepository.findById(any())).willReturn(Optional.ofNullable(club));
        BDDMockito.given(favoriteRepository.existsByUserAndClub(any(), any())).willReturn(false);

        // when
        favoriteService.addFavorite(user.getId(), club.getId());

        // then
        BDDMockito.verify(favoriteRepository, times(1)).save(any(Favorite.class));
    }

    @Test
    @DisplayName("즐겨찾기한 동아리를 삭제할 수 있다.")
    void deleteFavorite() {
        // given
        final User user = User.builder()
                .name("사용자 이름")
                .email("사용자 이메일")
                .grade("4")
                .department("사용자 학과")
                .studentId("사용자 학번")
                .build();
        ReflectionTestUtils.setField(user, "id", 1L);

        final Club club = Club.builder()
                .name("동아리 이름")
                .clubAffiliation(ClubAffiliation.CENTRAL_CLUB)
                .clubCategory(ClubCategory.CULTURAL_ART)
                .logo("동아리 로고")
                .description("동아리 설명")
                .instagram("동아리 인스타그램 링크")
                .build();
        ReflectionTestUtils.setField(club, "id", 1L);

        BDDMockito.given(userRepository.findById(any())).willReturn(Optional.ofNullable(user));
        BDDMockito.given(clubRepository.findById(any())).willReturn(Optional.ofNullable(club));
        BDDMockito.given(favoriteRepository.existsByUserAndClub(any(), any())).willReturn(true);

        // when
        favoriteService.deleteFavorite(user.getId(), club.getId());

        // then
        BDDMockito.verify(favoriteRepository, times(1)).deleteByUserAndClub(user, club);

    }

    @Test
    @DisplayName("즐겨찾기 시 이미 즐겨찾기가 되어 있는 경우 예외가 발생한다.")
    void foundPostLikWhenAdd() throws Exception {
        // given
        final User user = User.builder()
                .name("사용자 이름")
                .email("사용자 이메일")
                .grade("4")
                .department("사용자 학과")
                .studentId("사용자 학번")
                .build();
        ReflectionTestUtils.setField(user, "id", 1L);

        final Club club = Club.builder()
                .name("동아리 이름")
                .clubAffiliation(ClubAffiliation.CENTRAL_CLUB)
                .clubCategory(ClubCategory.CULTURAL_ART)
                .logo("동아리 로고")
                .description("동아리 설명")
                .instagram("동아리 인스타그램 링크")
                .build();
        ReflectionTestUtils.setField(club, "id", 1L);

        BDDMockito.given(userRepository.findById(any())).willReturn(Optional.ofNullable(user));
        BDDMockito.given(clubRepository.findById(any())).willReturn(Optional.ofNullable(club));
        BDDMockito.given(favoriteRepository.existsByUserAndClub(any(), any())).willReturn(true);

        // when, then
        Assertions.assertThatThrownBy(() -> favoriteService.addFavorite(user.getId(), club.getId()))
                .isInstanceOf(MokkojiException.class)
                .hasMessageContaining(FailMessage.CONFLICT_FAVORITE.getMessage());
    }

    @Test
    @DisplayName("즐겨찾기 삭제 시 해당하는 즐겨찾기 동아리가 없는 경우 예외가 발생한다.")
    void notFoundPostLikWhenDelete() throws Exception {
        // given
        final User user = User.builder()
                .name("사용자 이름")
                .email("사용자 이메일")
                .grade("4")
                .department("사용자 학과")
                .studentId("사용자 학번")
                .build();
        ReflectionTestUtils.setField(user, "id", 1L);

        final Club club = Club.builder()
                .name("동아리 이름")
                .clubAffiliation(ClubAffiliation.CENTRAL_CLUB)
                .clubCategory(ClubCategory.CULTURAL_ART)
                .logo("동아리 로고")
                .description("동아리 설명")
                .instagram("동아리 인스타그램 링크")
                .build();
        ReflectionTestUtils.setField(club, "id", 1L);

        BDDMockito.given(userRepository.findById(any())).willReturn(Optional.ofNullable(user));
        BDDMockito.given(clubRepository.findById(any())).willReturn(Optional.ofNullable(club));
        BDDMockito.given(favoriteRepository.existsByUserAndClub(any(), any())).willReturn(false);

        // when, then
        Assertions.assertThatThrownBy(() -> favoriteService.deleteFavorite(user.getId(), club.getId()))
                .isInstanceOf(MokkojiException.class)
                .hasMessageContaining(FailMessage.NOT_FOUND_FAVORITE.getMessage());
    }

    @Test
    @DisplayName("즐겨찾기한 동아리를 조회할 수 있다.")
    void findFavorites() {
        //given
        final User user = User.builder()
                .name("사용자 이름")
                .email("사용자 이메일")
                .grade("4")
                .department("사용자 학과")
                .studentId("사용자 학번")
                .build();
        ReflectionTestUtils.setField(user, "id", 1L);

        final Club club = Club.builder()
                .name("동아리 이름")
                .clubAffiliation(ClubAffiliation.CENTRAL_CLUB)
                .clubCategory(ClubCategory.CULTURAL_ART)
                .logo("동아리 로고")
                .description("동아리 설명")
                .instagram("동아리 인스타그램 링크")
                .build();
        ReflectionTestUtils.setField(club, "id", 1L);

        final Recruitment recruitment = Recruitment.builder().
                recruitStart(LocalDateTime.of(2025, 02, 01, 12, 00)).
                recruitEnd(LocalDateTime.of(2025, 03, 30, 12, 00)).
                content("동아리 모집 글").
                build();
        ReflectionTestUtils.setField(recruitment, "id", 1L);

        final List<Favorite> favorites = List.of(
                Favorite.builder().
                        club(club).
                        user(user).build()
        );

        BDDMockito.given(favoriteRepository.findByUserId(any())).willReturn(favorites);
        BDDMockito.given(recruitmentRepository.findByClubId(any())).willReturn(recruitment);
        BDDMockito.given(appDataS3Client.getPresignedUrl(any())).willReturn("testLogo1");

        //when
        final List<ClubResponse> favoriteClubs = favoriteService.findFavoriteClubs(user.getId());

        //then
        assertThat(favoriteClubs.size()).isEqualTo(1);
        assertThat(favoriteClubs.get(0).name()).isEqualTo("동아리 이름");
        assertThat(favoriteClubs.get(0).category()).isEqualTo("공연");
        assertThat(favoriteClubs.get(0).affiliation()).isEqualTo("중앙동아리");
        assertThat(favoriteClubs.get(0).description()).isEqualTo("동아리 설명");
        assertThat(favoriteClubs.get(0).recruitStartDate()).isEqualTo("2025-02-01");
        assertThat(favoriteClubs.get(0).recruitEndDate()).isEqualTo("2025-03-30");
        assertThat(favoriteClubs.get(0).imageURL()).isEqualTo("testLogo1");
        assertThat(favoriteClubs.get(0).isFavorite()).isEqualTo(true);

        BDDMockito.verify(favoriteRepository, times(1)).findByUserId(user.getId());
        BDDMockito.verify(recruitmentRepository, times(1)).findByClubId(club.getId());
    }
}
