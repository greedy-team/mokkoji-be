package com.greedy.mokkoji.api.user.service;

import com.greedy.mokkoji.api.auth.controller.argumentResolver.AuthCredential;
import com.greedy.mokkoji.api.auth.dto.TokenPair;
import com.greedy.mokkoji.api.jwt.JwtUtil;
import com.greedy.mokkoji.api.user.dto.resopnse.*;
import com.greedy.mokkoji.api.user.dto.resopnse.kakao.KakaoUserInfoResponse;
import com.greedy.mokkoji.api.user.service.kakao.KakaoSocialLoginService;
import com.greedy.mokkoji.common.exception.MokkojiException;
import com.greedy.mokkoji.db.club.repository.ClubRepository;
import com.greedy.mokkoji.db.clubapplication.repository.ClubApplicationRepository;
import com.greedy.mokkoji.db.clubmaster.repository.ClubMasterApplicationRepository;
import com.greedy.mokkoji.db.comment.repository.CommentRepository;
import com.greedy.mokkoji.db.favorite.repository.FavoriteRepository;
import com.greedy.mokkoji.db.university.entity.University;
import com.greedy.mokkoji.db.university.repository.UniversityRepository;
import com.greedy.mokkoji.db.user.entity.User;
import com.greedy.mokkoji.db.user.repository.UserRepository;
import com.greedy.mokkoji.enums.auth.AuthRole;
import com.greedy.mokkoji.enums.message.FailMessage;
import com.greedy.mokkoji.enums.university.UniversityCode;
import com.greedy.mokkoji.enums.user.UserRole;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
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
    private final UniversityRepository universityRepository;
    private final FavoriteRepository favoriteRepository;
    private final CommentRepository commentRepository;
    private final ClubApplicationRepository clubApplicationRepository;
    private final ClubMasterApplicationRepository clubMasterApplicationRepository;
    private final JwtUtil jwtUtil;
    private final TokenService tokenService;
    private final KakaoSocialLoginService kakaoSocialLoginService;

    public LoginResponse kakaoLogin(final String code, final String redirectUri, final UniversityCode universityCode) {
        final KakaoUserInfoResponse kakaoUserInfo = kakaoSocialLoginService.login(code, redirectUri);
        final String kakaoId = kakaoUserInfo.id();
        final String name = kakaoUserInfo.nickname();

        final Optional<User> existingUser = userRepository.findByKakaoId(kakaoId);
        final boolean isNewUser = existingUser.isEmpty();
        final User user = existingUser.orElseGet(
                () -> userRepository.save(
                        User.builder()
                                .university(findUniversityOrThrow(universityCode))
                                .code(RandomStringUtils.randomAlphanumeric(6))
                                .kakaoId(kakaoId)
                                .name(name)
                                .isEmailOn(true)
                                .role(UserRole.NORMAL)
                                .build()
                )
        );

        final TokenPair tokenPair = tokenService.issueTokens(AuthRole.USER, null, user.getId());
        return LoginResponse.of(tokenPair.accessToken(), tokenPair.refreshToken(), isNewUser);
    }

    @Transactional
    public String refreshAccessToken(String refreshToken) {
        final AuthCredential credential = jwtUtil.getCredentialFromToken(refreshToken);
        final Long userId = credential.accountId();

        String storedRefreshToken = tokenService.getRefreshToken(credential.authRole(), userId);
        if (storedRefreshToken == null || !storedRefreshToken.equals(refreshToken)) {
            throw new MokkojiException(FailMessage.UNAUTHORIZED);
        }

        return jwtUtil.generateAccessToken(credential);
    }

    @Transactional
    public void updateUserInformation(Long userId, String name, String email, Boolean isEmailOn, UniversityCode universityCode) {
        User user = findUser(userId);

        if (name != null) {
            user.updateName(name.isBlank() ? null : name);
        }

        if (email != null) {
            user.updateEmail(email.isBlank() ? null : email);
        }

        if (isEmailOn != null) {
            user.updateEmailOn(isEmailOn);
        }

        if (universityCode != null) {
            final University university = findUniversityOrThrow(universityCode);

            if (isDifferentUniversity(user, university)) {
                favoriteRepository.deleteByUserId(userId);
            }
            user.updateUniversity(university);
        }
    }

    @Transactional(readOnly = true)
    public User findUser(final Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new MokkojiException(FailMessage.NOT_FOUND_USER));
    }

    @Transactional(readOnly = true)
    public UserInformationResponse getUserInformation(final Long userId) {
        final User user = findUser(userId);
        return UserInformationResponse.of(user);
    }

    @Transactional
    public void logout(final AuthRole authRole, final Long userId) {
        tokenService.deleteRefreshToken(authRole, userId);
    }

    @Transactional
    public void deleteUser(final AuthRole authRole, final Long userId) {
        final User user = findUser(userId);

        clubRepository.findByMaster_Id(userId)
                .forEach(club -> club.updateMaster(null));

        favoriteRepository.deleteByUserId(userId);
        commentRepository.detachUserByUserId(userId);
        clubApplicationRepository.deleteByApplicantId(userId);
        clubMasterApplicationRepository.deleteByUserId(userId);

        tokenService.deleteRefreshToken(authRole, userId);

        userRepository.delete(user);
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

        List<UserManageClubResponse> clubs = clubRepository.findByMaster_Id(userId).stream()
                .map(club -> new UserManageClubResponse(club.getId(), club.getName()))
                .toList();

        return UserManageClubsResponse.of(clubs);
    }

    private University findUniversityOrThrow(final UniversityCode universityCode) {
        return universityRepository.findByCode(universityCode)
                .orElseThrow(() -> new MokkojiException(FailMessage.NOT_FOUND_UNIVERSITY));
    }

    private boolean isDifferentUniversity(final User user, final University university) {
        return !user.getUniversity().getId().equals(university.getId());
    }
}
