package com.greedy.mokkoji.api.user.service;

import com.greedy.mokkoji.api.external.SejongLoginClient;
import com.greedy.mokkoji.api.jwt.JwtUtil;
import com.greedy.mokkoji.api.user.dto.resopnse.StudentInformationExternalResponse;
import com.greedy.mokkoji.common.exception.MokkojiException;
import com.greedy.mokkoji.db.user.entity.User;
import com.greedy.mokkoji.db.user.repository.UserRepository;
import com.greedy.mokkoji.enums.message.FailMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final SejongLoginClient sejongLoginClient;
    private final JwtUtil jwtUtil;
    private final TokenService tokenService;

    //ToDo: 생 유저 정보를 넘기는 게 아니라 DTO처리해서 넘기는 것도 좋아보임
    @Transactional
    public User login(final String studentId, final String password) {

        final StudentInformationExternalResponse studentInformationExternalResponse = sejongLoginClient.getStudentInformation(studentId, password);

        return userRepository.findByStudentId(studentId).orElseGet(() -> {
            final User newUser = User.builder()
                    .studentId(studentId)
                    .name(studentInformationExternalResponse.name())
                    .department(studentInformationExternalResponse.department())
                    .grade(studentInformationExternalResponse.grade())
                    .build();

            return userRepository.save(newUser);
        });
    }

    public String refreshAccessToken(String refreshToken) {
        Long userId = jwtUtil.getUserIdFromToken(refreshToken);

        String storedRefreshToken = tokenService.getRefreshToken(userId);
        if (storedRefreshToken == null || !storedRefreshToken.equals(refreshToken)) {
            throw new MokkojiException(FailMessage.UNAUTHORIZED);
        }

        return jwtUtil.generateAccessToken(userId);
    }

    @Transactional
    public Optional<User> findUser(Long userId) {
        return userRepository.findById(userId);
    }
}

