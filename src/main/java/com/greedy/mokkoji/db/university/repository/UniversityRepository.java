package com.greedy.mokkoji.db.university.repository;

import com.greedy.mokkoji.db.university.entity.University;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface UniversityRepository extends JpaRepository<University, Long> {

}
