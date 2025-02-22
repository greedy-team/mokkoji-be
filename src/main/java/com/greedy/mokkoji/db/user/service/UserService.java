package com.greedy.mokkoji.db.user.service;

import com.greedy.mokkoji.api.auth.dto.StudentInformationResponseDto;
import com.greedy.mokkoji.db.user.entity.User;
import com.greedy.mokkoji.db.user.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
public class UserService {

    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public User findOrCreateUser(StudentInformationResponseDto response, String studentId) {
        return userRepository.findByStudentId(studentId).orElseGet(() -> {

            User newUser = User.builder()
                    .studentId(studentId)
                    .name(response.name())
                    .department(response.department())
                    .grade(response.grade())
                    .build();

            return userRepository.save(newUser);
        });
    }

    @Transactional
    public Optional<User> findUser(Long userId) {
        return userRepository.findById(userId);
    }
}

