package com.greedy.mokkoji.db.user.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
    private final ObjectMapper objectMapper;

    @Autowired
    public UserService(UserRepository userRepository, ObjectMapper objectMapper) {
        this.userRepository = userRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public void saveUser(String responseBody, String studentId) {
        boolean isUserExists = userRepository.existsById(Long.parseLong(studentId));
        if (isUserExists) {
            return;
        }
        try {
            JsonNode jsonResponse = objectMapper.readTree(responseBody);

            String name = jsonResponse.get("result").get("body").get("name").asText();
            String department = jsonResponse.get("result").get("body").get("major").asText();
            String grade = jsonResponse.get("result").get("body").get("grade").asText();

            User user = User.builder()
                    .studentId(studentId)
                    .name(name)
                    .department(department)
                    .grade(grade)
                    .build();

            userRepository.save(user);

        } catch (Exception e) {
            logger.error("Error saving user and creating profile: {}", e.getMessage(), e);
            throw new RuntimeException("사용자 정보 저장 중 오류가 발생했습니다.");
        }
    }
}
