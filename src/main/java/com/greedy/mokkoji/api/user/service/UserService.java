package com.greedy.mokkoji.api.user.service;

import com.greedy.mokkoji.api.auth.controller.argumentResolver.AuthCredential;
import com.greedy.mokkoji.api.jwt.JwtUtil;
import com.greedy.mokkoji.api.user.dto.resopnse.LoginResponse;
import com.greedy.mokkoji.api.user.dto.resopnse.UserManageClubResponse;
import com.greedy.mokkoji.api.user.dto.resopnse.UserManageClubsResponse;
import com.greedy.mokkoji.api.user.dto.resopnse.UserRoleResponse;
import com.greedy.mokkoji.api.user.dto.resopnse.kakao.KakaoUserInfoResponse;
import com.greedy.mokkoji.api.user.service.kakao.KakaoSocialLoginService;
import com.greedy.mokkoji.common.exception.MokkojiException;
import com.greedy.mokkoji.db.club.repository.ClubRepository;
import com.greedy.mokkoji.db.user.entity.User;
import com.greedy.mokkoji.db.user.repository.UserRepository;
import com.greedy.mokkoji.enums.auth.AuthRole;
import com.greedy.mokkoji.enums.message.FailMessage;
import com.greedy.mokkoji.enums.user.UserRole;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final ClubRepository clubRepository;
    private final JwtUtil jwtUtil;
    private final TokenService tokenService;
    private final KakaoSocialLoginService kakaoSocialLoginService;

    public LoginResponse kakaoLogin(final String code) {
        final KakaoUserInfoResponse kakaoUserInfo = kakaoSocialLoginService.login(code);
        final String kakaoId = kakaoUserInfo.id();

        final Optional<User> existingUser = userRepository.findByKakaoId(kakaoId);
        final boolean isNewUser = existingUser.isEmpty();
        final User user = existingUser.orElseGet(
                () -> userRepository.save(
                        User.builder()
                                .kakaoId(kakaoId)
                                .isEmailOn(true)
                                .role(UserRole.NORMAL)
                                .build()
                )
        );

        return tokenService.generateToken(AuthRole.USER, user.getId(), isNewUser);
    }

    @Transactional
    public String refreshAccessToken(String refreshToken) {
        final AuthCredential credential = jwtUtil.getCredentialFromToken(refreshToken);
        final Long userId = credential.userId();

        String storedRefreshToken = tokenService.getRefreshToken(userId);
        if (storedRefreshToken == null || !storedRefreshToken.equals(refreshToken)) {
            throw new MokkojiException(FailMessage.UNAUTHORIZED);
        }

        return jwtUtil.generateAccessToken(credential);
    }

    @Transactional
    public void updateUserInformation(Long userId, String email, Boolean isEmailOn) {
        User user = findUser(userId);

        if (email != null) {
            user.updateEmail(email.isBlank() ? null : email);
        }

        if (isEmailOn != null) {
            user.updateEmailOn(isEmailOn);
        }
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

    @Transactional(readOnly = true)
    public UserRoleResponse getUserRole(final Long userId) {
        final User user = userRepository.findById(userId)
                .orElseThrow(() -> new MokkojiException(FailMessage.NOT_FOUND_USER));

        return UserRoleResponse.of(user.getRole());
    }

    @Transactional
    public UserManageClubsResponse getUserManageClubs(final Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new MokkojiException(FailMessage.NOT_FOUND_USER);
        }

        List<UserManageClubResponse> clubs = clubRepository.findByMasterId(userId).stream()
                .map(club -> new UserManageClubResponse(club.getId(), club.getName()))
                .toList();

        return UserManageClubsResponse.of(clubs);
    }
}
