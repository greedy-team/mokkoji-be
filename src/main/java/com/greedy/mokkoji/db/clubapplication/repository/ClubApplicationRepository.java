package com.greedy.mokkoji.db.clubapplication.repository;

import com.greedy.mokkoji.db.clubapplication.entity.ClubApplication;
import com.greedy.mokkoji.db.university.entity.University;
import com.greedy.mokkoji.db.user.entity.User;
import com.greedy.mokkoji.enums.clubApplication.ClubApplicationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClubApplicationRepository extends JpaRepository<ClubApplication, Long>, ClubApplicationRepositoryCustom {

    boolean existsByApplicantAndUniversityAndStatusNot(User applicant, University university, ClubApplicationStatus status);

    List<ClubApplication> findByApplicant(User applicant);
}
