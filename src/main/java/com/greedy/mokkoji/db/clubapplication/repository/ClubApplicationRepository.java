package com.greedy.mokkoji.db.clubapplication.repository;

import com.greedy.mokkoji.db.clubapplication.entity.ClubApplication;
import com.greedy.mokkoji.db.university.entity.University;
import com.greedy.mokkoji.db.user.entity.User;
import com.greedy.mokkoji.enums.clubApplication.ClubApplicationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClubApplicationRepository extends JpaRepository<ClubApplication, Long> {

    boolean existsByApplicantAndUniversityAndStatusNot(User applicant, University university, ClubApplicationStatus status);
}
