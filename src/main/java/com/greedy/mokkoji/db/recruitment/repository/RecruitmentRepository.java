package com.greedy.mokkoji.db.recruitment.repository;

import com.greedy.mokkoji.db.club.entity.Club;
import com.greedy.mokkoji.db.recruitment.entity.Recruitment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RecruitmentRepository extends JpaRepository<Recruitment, Long> {
    Recruitment findByClub(final Club club);
}
