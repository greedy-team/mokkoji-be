package com.greedy.mokkoji.db.recruitment.repository;

import com.greedy.mokkoji.db.recruitment.entity.Recruitment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface RecruitmentRepository extends JpaRepository<Recruitment, Long> {
    //TODO:: query dsl로 변경
    @Query("SELECT r FROM Recruitment r WHERE FUNCTION('DATE', r.recruitStart) = :currentDate")
    List<Recruitment> findTodayRecruitStartDate(LocalDate currentDate);

    Recruitment findByClubId(final Long id);

    Optional<Recruitment> findRecruitmentById(Long id);

    List<Recruitment> findAllByClubId(final Long id);

    @Query("SELECT r FROM Recruitment r WHERE FUNCTION('DATE', r.recruitEnd) = :currentDate OR FUNCTION('DATE', r.recruitEnd) = :currentDate + 3")
    List<Recruitment> findAllByRecruitmentDeadlineTodayOrInThreeDays(LocalDate currentDate);
}
