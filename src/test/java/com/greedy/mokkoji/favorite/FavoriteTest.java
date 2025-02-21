package com.greedy.mokkoji.favorite;

import com.greedy.mokkoji.api.favorite.service.FavoriteService;
import com.greedy.mokkoji.common.exception.MokkojiException;
import com.greedy.mokkoji.db.club.entity.Club;
import com.greedy.mokkoji.db.club.repository.ClubRepository;
import com.greedy.mokkoji.db.favorite.entity.Favorite;
import com.greedy.mokkoji.db.favorite.repository.FavoriteRepository;
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

import java.util.Optional;

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

    @Test
    @DisplayName("동아리를 즐겨찾기할 수 있다.")
    void addFavorite() {
        // given
        final Long userId = 1L;
        final Long clubId = 1L;

        final User user = User.builder()
                .name("사용자 이름")
                .email("사용자 이메일")
                .grade(4)
                .department("사용자 학과")
                .studentId("사용자 학번")
                .build();

        final Club club = Club.builder()
                .name("동아리 이름")
                .clubAffiliation(ClubAffiliation.CENTRAL_CLUB)
                .clubCategory(ClubCategory.CULTURAL_ART)
                .logo("동아리 로고")
                .description("동아리 설명")
                .instagram("동아리 인스타그램 링크")
                .build();


        BDDMockito.given(userRepository.findById(any())).willReturn(Optional.ofNullable(user));
        BDDMockito.given(clubRepository.findById(any())).willReturn(Optional.ofNullable(club));
        BDDMockito.given(favoriteRepository.existsByUserAndClub(any(), any())).willReturn(false);

        // when
        favoriteService.addFavorite(userId, clubId);

        // then
        BDDMockito.verify(favoriteRepository, times(1)).save(any(Favorite.class));

    }

    @Test
    @DisplayName("즐겨찾기한 동아리를 삭제할 수 있다.")
    void deleteFavorite() {
        // given
        final Long userId = 1L;
        final Long clubId = 1L;

        final User user = User.builder()
                .name("사용자 이름")
                .email("사용자 이메일")
                .grade(4)
                .department("사용자 학과")
                .studentId("사용자 학번")
                .build();

        final Club club = Club.builder()
                .name("동아리 이름")
                .clubAffiliation(ClubAffiliation.CENTRAL_CLUB)
                .clubCategory(ClubCategory.CULTURAL_ART)
                .logo("동아리 로고")
                .description("동아리 설명")
                .instagram("동아리 인스타그램 링크")
                .build();

        BDDMockito.given(userRepository.findById(any())).willReturn(Optional.ofNullable(user));
        BDDMockito.given(clubRepository.findById(any())).willReturn(Optional.ofNullable(club));
        BDDMockito.given(favoriteRepository.existsByUserAndClub(any(), any())).willReturn(true);

        // when
        favoriteService.deleteFavorite(userId, clubId);

        // then
        BDDMockito.verify(favoriteRepository, times(1)).deleteByUserAndClub(user, club);

    }

    @Test
    @DisplayName("즐겨찾기 시 이미 즐겨찾기가 되어 있는 경우 예외가 발생한다.")
    void foundPostLikWhenAdd() throws Exception {
        // given
        final Long userId = 1L;
        final Long clubId = 1L;

        final User user = User.builder()
                .name("사용자 이름")
                .email("사용자 이메일")
                .grade(4)
                .department("사용자 학과")
                .studentId("사용자 학번")
                .build();

        final Club club = Club.builder()
                .name("동아리 이름")
                .clubAffiliation(ClubAffiliation.CENTRAL_CLUB)
                .clubCategory(ClubCategory.CULTURAL_ART)
                .logo("동아리 로고")
                .description("동아리 설명")
                .instagram("동아리 인스타그램 링크")
                .build();

        BDDMockito.given(userRepository.findById(any())).willReturn(Optional.ofNullable(user));
        BDDMockito.given(clubRepository.findById(any())).willReturn(Optional.ofNullable(club));
        BDDMockito.given(favoriteRepository.existsByUserAndClub(any(), any())).willReturn(true);

        // when, then
        Assertions.assertThatThrownBy(() -> favoriteService.addFavorite(userId, clubId))
                .isInstanceOf(MokkojiException.class)
                .hasMessageContaining(FailMessage.CONFLICT_FAVORITE.getMessage());
    }

    @Test
    @DisplayName("즐겨찾기 삭제 시 해당하는 즐겨찾기 동아리가 없는 경우 예외가 발생한다.")
    void notFoundPostLikWhenDelete() throws Exception {
        // given
        final Long userId = 1L;
        final Long clubId = 1L;

        final User user = User.builder()
                .name("사용자 이름")
                .email("사용자 이메일")
                .grade(4)
                .department("사용자 학과")
                .studentId("사용자 학번")
                .build();

        final Club club = Club.builder()
                .name("동아리 이름")
                .clubAffiliation(ClubAffiliation.CENTRAL_CLUB)
                .clubCategory(ClubCategory.CULTURAL_ART)
                .logo("동아리 로고")
                .description("동아리 설명")
                .instagram("동아리 인스타그램 링크")
                .build();

        BDDMockito.given(userRepository.findById(any())).willReturn(Optional.ofNullable(user));
        BDDMockito.given(clubRepository.findById(any())).willReturn(Optional.ofNullable(club));
        BDDMockito.given(favoriteRepository.existsByUserAndClub(any(), any())).willReturn(false);

        // when, then
        Assertions.assertThatThrownBy(() -> favoriteService.deleteFavorite(userId, clubId))
                .isInstanceOf(MokkojiException.class)
                .hasMessageContaining(FailMessage.NOT_FOUND_FAVORITE.getMessage());
    }

}
