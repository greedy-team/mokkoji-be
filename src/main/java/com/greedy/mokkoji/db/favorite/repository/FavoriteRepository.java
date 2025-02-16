package com.greedy.mokkoji.db.favorite.repository;

import com.greedy.mokkoji.db.club.entity.Club;
import com.greedy.mokkoji.db.favorite.entity.Favorite;
import com.greedy.mokkoji.db.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, Long> {
    @Query("SELECT f FROM Favorite f JOIN FETCH f.user WHERE f.club.id = :clubId")
    List<Favorite> findByClubIdWithFetchJoin(final Long clubId);

    boolean existsByUserAndClub(final User user, final Club club);

    void deleteByUserAndClub(final User user, final Club club);
}
