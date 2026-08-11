package com.greedy.mokkoji.db.club.repository;

import com.greedy.mokkoji.db.club.entity.Club;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClubRepository extends JpaRepository<Club, Long>, ClubRepositoryCustom {
    List<Club> findByMaster_Id(Long userId);

    boolean existsByMaster_Id(Long masterId);

    @Modifying
    @Query("UPDATE Club c SET c.viewCount = c.viewCount + 1 WHERE c.id = :clubId")
    void increaseViewCount(@Param("clubId") Long clubId);
}
