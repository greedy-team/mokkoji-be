package com.greedy.mokkoji.db.club.repository;

import com.greedy.mokkoji.db.club.entity.Club;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClubRepository extends JpaRepository<Club, Long>, ClubRepositoryCustom {
    List<Club> findByClubMasterStudentId(String studentId);

    boolean existsByClubMasterStudentId(String studentId);
}
