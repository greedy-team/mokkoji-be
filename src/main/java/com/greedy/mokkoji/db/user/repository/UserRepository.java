package com.greedy.mokkoji.db.user.repository;

import com.greedy.mokkoji.db.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    @Query("SELECT u FROM User u JOIN FETCH u.university WHERE u.kakaoId = :kakaoId")
    Optional<User> findByKakaoIdWithUniversity(@Param("kakaoId") String kakaoId);

    Optional<User> findByCode(String code);
}
