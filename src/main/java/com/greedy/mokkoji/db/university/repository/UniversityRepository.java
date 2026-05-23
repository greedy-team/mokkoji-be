package com.greedy.mokkoji.db.university.repository;

import com.greedy.mokkoji.db.university.entity.University;
import com.greedy.mokkoji.enums.university.UniversityCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UniversityRepository extends JpaRepository<University, Long> {
    Optional<University> findByCode(UniversityCode code);

    Optional<University> findByUniversityCode(UniversityCode universityCode);
}
