package com.greedy.mokkoji.db.recruitment.repository;

import com.greedy.mokkoji.db.recruitment.entity.Recruitment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface RecruitmentRepository extends JpaRepository<Recruitment, Long>, RecruitmentRepositoryCustom {
    @Query("SELECT r FROM Recruitment r WHERE FUNCTION('DATE', r.recruitStart) = :currentDate")
    List<Recruitment> findAllByRecruitStartToday(LocalDate currentDate);

    // 2. 모집 마감일이 오늘인 경우
    @Query("SELECT r FROM Recruitment r WHERE FUNCTION('DATE', r.recruitEnd) = :currentDate")
    List<Recruitment> findAllByRecruitEndToday(LocalDate currentDate);

    // 3. 모집 마감일이 오늘로부터 3일 전인 경우
    @Query("SELECT r FROM Recruitment r WHERE FUNCTION('DATE', r.recruitEnd) = :targetDate")
    List<Recruitment> findAllByRecruitEndInThreeDays(@Param("targetDate") LocalDate targetDate);


    Recruitment findByClubId(final Long id);

    Optional<Recruitment> findRecruitmentById(Long id);

    List<Recruitment> findAllByClubId(final Long id);

    List<Recruitment> findByClubIdIn(List<Long> clubIds);

    Optional<Recruitment> findTopByClubIdOrderByUpdatedAtDesc(Long clubId);
}
