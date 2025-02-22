package com.greedy.mokkoji.db.user.service;

import com.greedy.mokkoji.api.auth.dto.StudentInformationResponseDto;
import com.greedy.mokkoji.db.user.entity.User;
import com.greedy.mokkoji.db.user.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public User findOrCreateUser(StudentInformationResponseDto response, String studentId) {
        return userRepository.findByStudentId(studentId).orElseGet(() -> {
            try {
                User newUser = User.builder()
                        .studentId(studentId)
                        .name(response.name())
                        .department(response.department())
                        .grade(response.grade())
                        .build();
                return userRepository.save(newUser);
            } catch (Exception e) {
                logger.error("사용자 정보 저장 중 오류 발생: studentId={}", studentId, e);
                throw new RuntimeException("사용자 정보 저장 실패: " + e.getMessage(), e);
            }
        });
    }

    @Transactional
    public Optional<User> findUser(Long userId) {
        return userRepository.findById(userId);
    }
}

