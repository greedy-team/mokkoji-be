package com.greedy.mokkoji.db.club.repository;

import com.greedy.mokkoji.db.club.entity.Club;
import com.greedy.mokkoji.enums.RecruitStatus;
import com.greedy.mokkoji.enums.club.ClubAffiliation;
import com.greedy.mokkoji.enums.club.ClubCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ClubRepositoryCustom {
    Page<Club> findClubs(final String keyword,
                         final ClubCategory category,
                         final ClubAffiliation affiliation,
                         final RecruitStatus status,
                         final Pageable pageable);
}
