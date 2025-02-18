package com.greedy.mokkoji.db.club.repository;

import com.greedy.mokkoji.db.club.entity.Club;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClubRepository extends JpaRepository<Club, Long>, ClubRepositoryCustom {
}
