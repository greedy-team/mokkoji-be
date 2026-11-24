package com.greedy.mokkoji.db.clubapplication.repository;

import com.greedy.mokkoji.db.clubapplication.entity.ClubApplication;
import com.greedy.mokkoji.db.university.entity.University;
import com.greedy.mokkoji.db.user.entity.User;
import com.greedy.mokkoji.enums.application.ApplicationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClubApplicationRepository extends JpaRepository<ClubApplication, Long>, ClubApplicationRepositoryCustom {

    boolean existsByApplicantAndUniversityAndClubNameAndStatusNot(User applicant, University university, String clubName, ApplicationStatus status);

    @Query("SELECT a FROM ClubApplication a JOIN FETCH a.university WHERE a.applicant = :applicant")
    List<ClubApplication> findByApplicant(@Param("applicant") User applicant);

    void deleteByApplicantId(final Long applicantId);
}
