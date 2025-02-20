package com.greedy.mokkoji.db.favorite.repository;

import com.greedy.mokkoji.db.club.entity.Club;
import com.greedy.mokkoji.db.favorite.entity.Favorite;
import com.greedy.mokkoji.db.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, Long> {
    //TODO:: query dsl로 변경
    @Query("SELECT f FROM Favorite f JOIN FETCH f.user WHERE f.club.id = :clubId")
    List<Favorite> findByClubIdWithFetchJoin(final Long clubId);

    boolean existsByUserAndClub(final User user, final Club club);

    void deleteByUserAndClub(final User user, final Club club);

    boolean existsByUserIdAndClubId(final Long userId, final Long id);
}
