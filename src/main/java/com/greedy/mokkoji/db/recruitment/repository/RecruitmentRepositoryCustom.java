package com.greedy.mokkoji.db.recruitment.repository;

import com.greedy.mokkoji.db.recruitment.entity.Recruitment;
import com.greedy.mokkoji.enums.club.ClubAffiliation;
import com.greedy.mokkoji.enums.club.ClubCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface RecruitmentRepositoryCustom {
    Page<Recruitment> findRecruitments(
            final ClubAffiliation affiliation,
            final ClubCategory category,
            final Pageable pageable
    );
}
