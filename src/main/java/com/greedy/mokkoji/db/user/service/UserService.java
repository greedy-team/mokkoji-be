package com.greedy.mokkoji.db.user.service;

import com.greedy.mokkoji.api.login.dto.StudentInformationResponseDto;
import com.greedy.mokkoji.db.user.entity.User;
import com.greedy.mokkoji.db.user.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public void saveUser(StudentInformationResponseDto response, String studentId) {
        boolean isUserExists = userRepository.existsByStudentId(studentId);
        if (isUserExists) {
            return;
        }
        try {
            String name = response.getName();
            String department = response.getDepartment();
            String grade = response.getGrade();

            User user = new User(studentId, name, department, grade);

            userRepository.save(user);

        } catch (Exception e) {
            logger.error("Error saving user and creating profile: {}", e.getMessage(), e);
            throw new RuntimeException("사용자 정보 저장 중 오류가 발생했습니다.");
        }
    }
}
