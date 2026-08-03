package com.greedy.mokkoji.common;

import com.greedy.mokkoji.db.club.repository.ClubRepository;
import com.greedy.mokkoji.db.comment.repository.CommentRepository;
import com.greedy.mokkoji.db.favorite.repository.FavoriteRepository;
import com.greedy.mokkoji.db.recruitment.repository.RecruitmentImageRepository;
import com.greedy.mokkoji.db.recruitment.repository.RecruitmentRepository;
import com.greedy.mokkoji.db.university.repository.UniversityRepository;
import com.greedy.mokkoji.db.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;

@ActiveProfiles("test")
public class AbstractTest {

    // 통합 테스트 전용 MySQL 컨테이너(dev와 동일한 MySQL 엔진).
    protected static final MySQLContainer<?> MYSQL = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("mokkoji_test")
            .withUsername("test")
            .withPassword("test");

    static {
        MYSQL.start();
    }

    @DynamicPropertySource
    static void datasourceProperties(final DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", MYSQL::getJdbcUrl);
        registry.add("spring.datasource.username", MYSQL::getUsername);
        registry.add("spring.datasource.password", MYSQL::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "com.mysql.cj.jdbc.Driver");
    }

    // 공유 컨테이너라 클래스 간 데이터가 남으므로, 매 테스트 전에 FK 자식 테이블부터 역순으로 정리한다.
    @BeforeEach
    void cleanUpDatabase() {
        recruitmentImageRepository.deleteAll();
        favoriteRepository.deleteAll();
        commentRepository.deleteAll();
        recruitmentRepository.deleteAll();
        clubRepository.deleteAll();
        userRepository.deleteAll();
        universityRepository.deleteAll();
    }

    @Autowired
    protected UserRepository userRepository;

    @Autowired
    protected UniversityRepository universityRepository;

    @Autowired
    protected ClubRepository clubRepository;

    @Autowired
    protected FavoriteRepository favoriteRepository;

    @Autowired
    protected RecruitmentRepository recruitmentRepository;

    @Autowired
    protected RecruitmentImageRepository recruitmentImageRepository;

    @Autowired
    protected CommentRepository commentRepository;
}
