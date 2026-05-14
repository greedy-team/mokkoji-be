package com.greedy.mokkoji.db.club.repository;

import com.greedy.mokkoji.api.club.dto.response.allClubs.ClubWithLatestRecruitment;
import com.greedy.mokkoji.db.club.entity.Club;
import com.greedy.mokkoji.enums.club.ClubAffiliation;
import com.greedy.mokkoji.enums.club.ClubCategory;
import com.greedy.mokkoji.enums.recruitment.RecruitStatus;
import com.greedy.mokkoji.enums.university.UniversityCode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ClubRepositoryCustom {
    Page<Club> findClubsWithLatestRecruitment(
            final UniversityCode universityCode,
            final String keyword,
            final ClubCategory category,
            final ClubAffiliation affiliation,
            final RecruitStatus status,
            final Pageable pageable
            );

    List<ClubWithLatestRecruitment> findAllClubsWithLatestRecruitment(
            final UniversityCode universityCode,
            final String keyword,
            final ClubAffiliation affiliation,
            final ClubCategory category
            );
}
