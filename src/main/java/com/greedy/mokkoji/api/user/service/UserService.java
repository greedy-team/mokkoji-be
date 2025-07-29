package com.greedy.mokkoji.api.user.service;

import com.greedy.mokkoji.api.jwt.JwtUtil;
import com.greedy.mokkoji.api.user.dto.resopnse.KakaoUserInfoResponse;
import com.greedy.mokkoji.common.exception.MokkojiException;
import com.greedy.mokkoji.db.user.entity.User;
import com.greedy.mokkoji.db.user.repository.UserRepository;
import com.greedy.mokkoji.enums.message.FailMessage;
import com.greedy.mokkoji.enums.user.UserRole;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final TokenService tokenService;
    private final KakaoSocialLoginService kakaoSocialLoginService;

    //ToDo: 생 유저 정보를 넘기는 게 아니라 DTO처리해서 넘기는 것도 좋아보임
    @Transactional
    public User login(final String code) {
        final KakaoUserInfoResponse userInfo = kakaoSocialLoginService.login(code);

        return userRepository.findByUniqueId(userInfo.id()).orElseGet(() -> {
            final User newUser = User.builder()
                .uniqueId(userInfo.id())
                .nickname(userInfo.kakaoAccount().profile().nickname())
                .role(UserRole.NORMAL)
                .build();
            return userRepository.save(newUser);
        });
    }

    public String refreshAccessToken(String refreshToken) {
        final Long userId = jwtUtil.getUserIdFromToken(refreshToken);

        String storedRefreshToken = tokenService.getRefreshToken(userId);
        if (storedRefreshToken == null || !storedRefreshToken.equals(refreshToken)) {
            throw new MokkojiException(FailMessage.UNAUTHORIZED);
        }

        return jwtUtil.generateAccessToken(userId);
    }

    @Transactional
    public void updateEmail(final Long userId, final String email) {
        final User user = findUser(userId);
        user.updateEmail(email);
    }

    @Transactional(readOnly = true)
    public User findUser(final Long userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> new MokkojiException(FailMessage.NOT_FOUND_USER));
    }

    @Transactional
    public void logOut(final Long userId) {
        tokenService.deleteRefreshToken(userId);
    }
}

