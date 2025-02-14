package com.greedy.mokkoji.api.favorite.service;

import com.greedy.mokkoji.common.exception.MokkojiException;
import com.greedy.mokkoji.db.club.entity.Club;
import com.greedy.mokkoji.db.club.repository.ClubRepository;
import com.greedy.mokkoji.db.favorite.entity.Favorite;
import com.greedy.mokkoji.db.favorite.repository.FavoriteRepository;
import com.greedy.mokkoji.db.user.entity.User;
import com.greedy.mokkoji.db.user.repository.UserRepository;
import com.greedy.mokkoji.enums.message.FailMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final UserRepository userRepository;
    private final ClubRepository clubRepository;

    @Transactional
    public Void addFavorite(final Long userId, final Long clubId) {

        final User user = userRepository.findById(userId).orElseThrow(
                () -> new MokkojiException(FailMessage.NOT_FOUND_USER)
        );
        final Club club = clubRepository.findById(clubId).orElseThrow(
                () -> new MokkojiException(FailMessage.NOT_FOUND_CLUB)
        );

        if (favoriteRepository.existsByUserAndClub(user, club)) {
            throw new MokkojiException(FailMessage.CONFLICT_FAVORITE);
        }

        favoriteRepository.save(
                Favorite.builder()
                        .user(user)
                        .club(club)
                        .build()
        );

        return null;
    }

    @Transactional
    public Void deleteFavorite(final Long userId, final Long clubId) {

        final User user = userRepository.findById(userId).orElseThrow(
                () -> new MokkojiException(FailMessage.NOT_FOUND_USER)
        );
        final Club club = clubRepository.findById(clubId).orElseThrow(
                () -> new MokkojiException(FailMessage.NOT_FOUND_CLUB)
        );

        if (!favoriteRepository.existsByUserAndClub(user, club)) {
            throw new MokkojiException(FailMessage.NOT_FOUND_FAVORITE);
        }

        favoriteRepository.deleteByUserAndClub(user, club);

        return null;
    }
}
