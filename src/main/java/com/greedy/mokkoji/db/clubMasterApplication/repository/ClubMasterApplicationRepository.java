package com.greedy.mokkoji.db.clubMasterApplication.repository;

import com.greedy.mokkoji.db.clubMasterApplication.entity.ClubMasterApplication;
import com.greedy.mokkoji.db.university.entity.University;
import com.greedy.mokkoji.db.user.entity.User;
import com.greedy.mokkoji.enums.application.ApplicationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ClubMasterApplicationRepository extends JpaRepository<ClubMasterApplication, Long> {
    List<ClubMasterApplication> findByUserIdOrderByCreatedAtDesc(Long userId);

    Page<ClubMasterApplication> findByUniversityIdOrderByCreatedAtAsc(Long universityId, Pageable pageable);

    Page<ClubMasterApplication> findAllByOrderByCreatedAtAsc(Pageable pageable);

    boolean existsByUserAndUniversityAndStatusNot(User user, University university, ApplicationStatus status);
}
