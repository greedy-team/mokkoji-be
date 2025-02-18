package com.greedy.mokkoji.db.favorite.repository;

import com.greedy.mokkoji.db.favorite.entity.Favorite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, Long> {
    @Query("SELECT f.club.id FROM Favorite f WHERE f.user.id = :userId")
    List<Long> findClubIdByUserId(@Param("userId") final Long userId);
}
