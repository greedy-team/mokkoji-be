package com.greedy.mokkoji.db.recruitment.repository;

import com.greedy.mokkoji.db.recruitment.entity.Recruitment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface RecruitmentRepository extends JpaRepository<Recruitment, Long>, RecruitmentRepositoryCustom {

    @Query("SELECT r FROM Recruitment r JOIN FETCH r.club c JOIN FETCH c.university WHERE r.recruitStart >= :from AND r.recruitStart < :to")
    List<Recruitment> findAllByRecruitStartToday(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    @Query("SELECT r FROM Recruitment r JOIN FETCH r.club c JOIN FETCH c.university WHERE r.recruitEnd >= :from AND r.recruitEnd < :to")
    List<Recruitment> findAllByRecruitEndToday(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    @Query("SELECT r FROM Recruitment r JOIN FETCH r.club c JOIN FETCH c.university WHERE r.recruitEnd >= :from AND r.recruitEnd < :to")
    List<Recruitment> findAllByRecruitEndInThreeDays(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    Optional<Recruitment> findRecruitmentById(Long id);

    List<Recruitment> findAllByClubId(final Long id);

    @Query("SELECT r.id FROM Recruitment r WHERE r.club.id = :clubId")
    List<Long> findIdsByClubId(final Long clubId);

    @Modifying
    @Query("DELETE FROM Recruitment r WHERE r.club.id = :clubId")
    void deleteByClubId(final Long clubId);

    Optional<Recruitment> findTopByClubIdOrderByCreatedAtDesc(Long clubId);
}
