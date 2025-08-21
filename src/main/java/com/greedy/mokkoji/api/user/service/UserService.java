package com.greedy.mokkoji.api.user.service;

import com.greedy.mokkoji.api.external.sejong.SejongLoginRestClient;
import com.greedy.mokkoji.api.jwt.JwtUtil;
import com.greedy.mokkoji.api.user.dto.resopnse.StudentInformationResponse;
import com.greedy.mokkoji.api.user.dto.resopnse.UserManageClubResponse;
import com.greedy.mokkoji.api.user.dto.resopnse.UserManageClubsResponse;
import com.greedy.mokkoji.api.user.dto.resopnse.UserRoleResponse;
import com.greedy.mokkoji.common.exception.MokkojiException;
import com.greedy.mokkoji.db.club.repository.ClubRepository;
import com.greedy.mokkoji.db.user.entity.User;
import com.greedy.mokkoji.db.user.repository.UserRepository;
import com.greedy.mokkoji.enums.message.FailMessage;
import com.greedy.mokkoji.enums.user.UserRole;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final ClubRepository clubRepository;
    private final JwtUtil jwtUtil;
    private final TokenService tokenService;
    private final SejongLoginRestClient sejongLoginClient;

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
                    .role(determineUserRole(studentId))
                    .build();

            return userRepository.save(newUser);
        });
    }

    private UserRole determineUserRole(final String studentId) {
        if (clubRepository.existsByClubMasterStudentId(studentId)) {
            return UserRole.CLUB_MASTER;
        }
        return UserRole.NORMAL;
    }

    @Transactional
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

    @Transactional(readOnly = true)
    public UserRoleResponse getUserRole(final Long userId) {
        final User user = userRepository.findById(userId)
                .orElseThrow(() -> new MokkojiException(FailMessage.NOT_FOUND_USER));

        return UserRoleResponse.of(
                user.getRole().toString()
        );
    }

    @Transactional
    public UserManageClubsResponse getUserManageClubs(final Long userId) {
        final User user = userRepository.findById(userId)
                .orElseThrow(() -> new MokkojiException(FailMessage.NOT_FOUND_USER));

        String studentId = user.getStudentId();

        List<UserManageClubResponse> clubs = clubRepository.findByClubMasterStudentId(studentId).stream()
                .map(club -> new UserManageClubResponse(club.getId(), club.getName()))
                .toList();

        return UserManageClubsResponse.of(clubs);
    }
}

