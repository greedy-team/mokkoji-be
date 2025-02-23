package com.greedy.mokkoji.api.user.service;

import com.greedy.mokkoji.MokkojiApplication;
import com.greedy.mokkoji.api.external.SejongLoginClient;
import com.greedy.mokkoji.api.jwt.JwtUtil;
import com.greedy.mokkoji.api.user.dto.resopnse.StudentInformationResponse;
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

        final StudentInformationResponse studentInformationResponse = sejongLoginClient.getStudentInformation(studentId, password);

        return userRepository.findByStudentId(studentId).orElseGet(() -> {
            final User newUser = User.builder()
                    .studentId(studentId)
                    .name(studentInformationResponse.name())
                    .department(studentInformationResponse.department())
                    .grade(studentInformationResponse.grade())
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
    public void updateEmail(Long userId, String email){
        User user = findUser(userId);
        user.updateEmail(email);
    }

    @Transactional
    public User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new MokkojiException(FailMessage.NOT_FOUND_USER));
    }
}

