package com.greedy.mokkoji.api.admin.service;

import com.greedy.mokkoji.api.admin.dto.response.AdminLoginResponse;
import com.greedy.mokkoji.api.auth.dto.TokenPair;
import com.greedy.mokkoji.api.user.service.TokenService;
import com.greedy.mokkoji.common.exception.MokkojiException;
import com.greedy.mokkoji.db.admin.entity.Admin;
import com.greedy.mokkoji.db.admin.repository.AdminRepository;
import com.greedy.mokkoji.enums.auth.AuthRole;
import com.greedy.mokkoji.enums.message.FailMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminAuthService {

    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;

    @Transactional(readOnly = true)
    public AdminLoginResponse login(final String loginId, final String password) {
        final Admin admin = adminRepository.findByLoginId(loginId)
                .orElseThrow(() -> new MokkojiException(FailMessage.UNAUTHORIZED_ADMIN_LOGIN));

        if (!passwordEncoder.matches(password, admin.getPassword())) {
            throw new MokkojiException(FailMessage.UNAUTHORIZED_ADMIN_LOGIN);
        }

        final TokenPair tokenPair = tokenService.issueTokens(AuthRole.ADMIN, admin.getId());
        return AdminLoginResponse.of(tokenPair.accessToken(), tokenPair.refreshToken());
    }
}
