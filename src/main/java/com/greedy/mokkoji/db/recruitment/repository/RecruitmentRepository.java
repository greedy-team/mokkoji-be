package com.greedy.mokkoji.db.recruitment.repository;

import com.greedy.mokkoji.db.recruitment.entity.Recruitment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface RecruitmentRepository extends JpaRepository<Recruitment, Long>, RecruitmentRepositoryCustom {

    @Query("SELECT r FROM Recruitment r JOIN FETCH r.club c JOIN FETCH c.university WHERE FUNCTION('DATE', r.recruitStart) = :currentDate")
    List<Recruitment> findAllByRecruitStartToday(LocalDate currentDate);

    @Query("SELECT r FROM Recruitment r JOIN FETCH r.club c JOIN FETCH c.university WHERE FUNCTION('DATE', r.recruitEnd) = :currentDate")
    List<Recruitment> findAllByRecruitEndToday(LocalDate currentDate);

    @Query("SELECT r FROM Recruitment r JOIN FETCH r.club c JOIN FETCH c.university WHERE FUNCTION('DATE', r.recruitEnd) = :targetDate")
    List<Recruitment> findAllByRecruitEndInThreeDays(@Param("targetDate") LocalDate targetDate);

    Optional<Recruitment> findRecruitmentById(Long id);

    List<Recruitment> findAllByClubId(final Long id);

    @Query("SELECT r.id FROM Recruitment r WHERE r.club.id = :clubId")
    List<Long> findIdsByClubId(final Long clubId);

    @Modifying
    @Query("DELETE FROM Recruitment r WHERE r.club.id = :clubId")
    void deleteByClubId(final Long clubId);

    Optional<Recruitment> findTopByClubIdOrderByCreatedAtDesc(Long clubId);
}
