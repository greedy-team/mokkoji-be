package com.greedy.mokkoji.db.favorite.repository;

import com.greedy.mokkoji.db.favorite.entity.Favorite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, Long> {
}
