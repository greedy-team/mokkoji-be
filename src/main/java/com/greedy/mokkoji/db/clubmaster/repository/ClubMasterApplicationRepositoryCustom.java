package com.greedy.mokkoji.db.clubmaster.repository;

import com.greedy.mokkoji.db.clubmaster.entity.ClubMasterApplication;
import com.greedy.mokkoji.enums.application.ApplicationStatus;
import com.greedy.mokkoji.enums.club.ClubAffiliation;
import com.greedy.mokkoji.enums.university.UniversityCode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ClubMasterApplicationRepositoryCustom {
    Page<ClubMasterApplication> findByConditions(UniversityCode universityCode, ApplicationStatus status, ClubAffiliation affiliation, Pageable pageable);
}
