package com.greedy.mokkoji.db.recruitment.repository;

import com.greedy.mokkoji.db.recruitment.entity.Recruitment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface RecruitmentRepository extends JpaRepository<Recruitment, Long> {
    @Query("SELECT r FROM Recruitment r WHERE FUNCTION('DATE', r.recruitStart) = :currentDate")
    List<Recruitment> findTodayRecruitStartDate(LocalDate currentDate);
}
