package com.greedy.mokkoji.db.clubapplication.repository;

import com.greedy.mokkoji.db.clubapplication.entity.ClubApplication;
import com.greedy.mokkoji.enums.application.ApplicationStatus;
import com.greedy.mokkoji.enums.club.ClubAffiliation;
import com.greedy.mokkoji.enums.university.UniversityCode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ClubApplicationRepositoryCustom {

    Page<ClubApplication> findByConditions(UniversityCode universityCode, ApplicationStatus status, ClubAffiliation affiliation, Pageable pageable);
}
