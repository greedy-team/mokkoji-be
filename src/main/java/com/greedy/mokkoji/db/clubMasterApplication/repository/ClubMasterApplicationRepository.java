package com.greedy.mokkoji.db.clubMasterApplication.repository;

import com.greedy.mokkoji.db.clubMasterApplication.entity.ClubMasterApplication;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ClubMasterApplicationRepository extends JpaRepository<ClubMasterApplication, Long> {
    List<ClubMasterApplication> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<ClubMasterApplication> findByUniversityIdOrderByCreatedAtAsc(Long universityId);
}
