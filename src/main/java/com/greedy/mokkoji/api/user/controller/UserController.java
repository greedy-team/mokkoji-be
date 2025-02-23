package com.greedy.mokkoji.api.user.controller;

import com.greedy.mokkoji.api.auth.controller.argumentResolver.AuthCredential;
import com.greedy.mokkoji.api.auth.controller.argumentResolver.Authentication;
import com.greedy.mokkoji.api.jwt.JwtUtil;
import com.greedy.mokkoji.api.user.dto.request.LoginRequest;
import com.greedy.mokkoji.api.user.dto.resopnse.LoginResponse;
import com.greedy.mokkoji.api.user.dto.resopnse.RefreshResponse;
import com.greedy.mokkoji.api.user.service.TokenService;
import com.greedy.mokkoji.api.user.service.UserService;
import com.greedy.mokkoji.common.exception.MokkojiException;
import com.greedy.mokkoji.common.response.APISuccessResponse;
import com.greedy.mokkoji.db.user.entity.User;
import com.greedy.mokkoji.enums.message.FailMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("${api.prefix}/users")
public class UserController {

    private final UserService userService;
    private final TokenService tokenService;
    private final JwtUtil jwtUtil;

    @PostMapping("/auth/login")
    public ResponseEntity<APISuccessResponse<LoginResponse>> login(@RequestBody LoginRequest request) {
        final User user = userService.login(request.studentId(), request.password());
        final LoginResponse loginResponse = tokenService.generateToken(user.getId());

        return APISuccessResponse.of(HttpStatus.OK, loginResponse);
    }

    @PostMapping("/auth/refresh")
    public ResponseEntity<APISuccessResponse<RefreshResponse>> refresh(@RequestHeader("Authorization") String refreshToken) {
        if (refreshToken == null || refreshToken.isEmpty()) {
            throw new MokkojiException(FailMessage.UNAUTHORIZED);
        }

        refreshToken = refreshToken.replace("Bearer ", "");

        Long userId = jwtUtil.getUserIdFromToken(refreshToken);

        String storedRefreshToken = tokenService.getRefreshToken(userId);

        if (storedRefreshToken == null || !storedRefreshToken.equals(refreshToken)) {
            throw new MokkojiException(FailMessage.UNAUTHORIZED);
        }

        String newAccessToken = jwtUtil.generateAccessToken(userId);
        RefreshResponse refreshResponse = RefreshResponse.of(newAccessToken);

        return APISuccessResponse.of(HttpStatus.OK, refreshResponse);
    }

    @PostMapping("/auth/logout")
    public ResponseEntity<Void> logout(
            @Authentication AuthCredential authCredential
    ) {
        Long userId = authCredential.userId();

        tokenService.deleteRefreshToken(userId);

        return ResponseEntity.noContent().build();
    }
}
