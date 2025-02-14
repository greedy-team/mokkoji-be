package com.greedy.mokkoji.db.favorite.repository;

import com.greedy.mokkoji.db.club.entity.Club;
import com.greedy.mokkoji.db.favorite.entity.Favorite;
import com.greedy.mokkoji.db.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, Long> {

    boolean existsByUserAndClub(final User user, final Club club);

    void deleteByUserAndClub(final User user, final Club club);
}
