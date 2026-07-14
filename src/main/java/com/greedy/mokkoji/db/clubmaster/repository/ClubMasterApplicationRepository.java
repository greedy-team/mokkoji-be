package com.greedy.mokkoji.db.clubmaster.repository;

import com.greedy.mokkoji.db.club.entity.Club;
import com.greedy.mokkoji.db.clubmaster.entity.ClubMasterApplication;
import com.greedy.mokkoji.db.user.entity.User;
import com.greedy.mokkoji.enums.application.ApplicationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ClubMasterApplicationRepository extends JpaRepository<ClubMasterApplication, Long>, ClubMasterApplicationRepositoryCustom {
    List<ClubMasterApplication> findByUserIdOrderByCreatedAtDesc(Long userId);

    boolean existsByUserAndClubAndStatusNot(User user, Club club, ApplicationStatus status);

    void deleteByUserId(final Long userId);

    @Modifying
    @Query("DELETE FROM ClubMasterApplication cma WHERE cma.club.id = :clubId")
    void deleteByClubId(final Long clubId);
}
