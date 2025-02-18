package com.greedy.mokkoji.db.club.repository;

import com.greedy.mokkoji.api.club.dto.club.ClubSearchCond;
import com.greedy.mokkoji.db.club.entity.Club;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ClubRepositoryCustom {
    Page<Club> findClubs(final ClubSearchCond cond, final Pageable pageable);
}
