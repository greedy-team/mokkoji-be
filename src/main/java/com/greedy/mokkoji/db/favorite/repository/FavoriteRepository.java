package com.greedy.mokkoji.db.favorite.repository;

import com.greedy.mokkoji.db.favorite.entity.Favorite;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FavoriteRepository extends JpaRepository<Favorite, Long> {
}
