
package com.greedy.mokkoji.api.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.greedy.mokkoji.api.auth.dto.LoginRequestDto;
import com.greedy.mokkoji.api.auth.dto.LoginResponseDto;
import com.greedy.mokkoji.api.auth.dto.RefreshResponseDto;
import com.greedy.mokkoji.api.auth.dto.StudentInformationResponseDto;
import com.greedy.mokkoji.api.auth.service.LoginService;
import com.greedy.mokkoji.api.auth.service.TokenService;
import com.greedy.mokkoji.api.jwt.JwtUtil;
import com.greedy.mokkoji.common.exception.MokkojiException;
import com.greedy.mokkoji.common.response.APISuccessResponse;
import com.greedy.mokkoji.db.user.entity.User;
import com.greedy.mokkoji.db.user.service.UserService;
import com.greedy.mokkoji.enums.message.FailMessage;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;


@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/auth")
public class AuthController {
    private final UserService userService;
    private final LoginService loginService;
    private final TokenService tokenService;
    private final ObjectMapper mapper;
    private final JwtUtil jwtUtil;

    @PostMapping("/login")
    public ResponseEntity<APISuccessResponse<LoginResponseDto>> login(@RequestBody LoginRequestDto request) {
        String studentId = request.getStudentId();
        String password = request.getPassword();

        StudentInformationResponseDto response = loginService.getStudentInformation(studentId, password);

        User user = userService.findOrCreateUser(response, studentId);

        String accessToken = jwtUtil.generateAccessToken(user.getId());
        String refreshToken = jwtUtil.generateRefreshToken(user.getId());

        tokenService.saveRefreshToken(user.getId(), refreshToken);

        LoginResponseDto tokenResponse = new LoginResponseDto(accessToken, refreshToken);
        return APISuccessResponse.of(HttpStatus.OK, tokenResponse);
    }

    @PostMapping("/refresh")
    public ResponseEntity<APISuccessResponse<RefreshResponseDto>> refresh(@RequestHeader("Authorization") String refreshToken) {
        if (refreshToken == null || refreshToken.isEmpty()) {
            throw new MokkojiException(FailMessage.UNAUTHORIZED);
        }

        refreshToken = refreshToken.replace("Bearer ", "");

        try {
            Long userId = jwtUtil.getUserIdFromToken(refreshToken);

            String storedRefreshToken = tokenService.getRefreshToken(userId);
            if (storedRefreshToken == null || !storedRefreshToken.equals(refreshToken)) {
                throw new MokkojiException(FailMessage.UNAUTHORIZED);
            }

            String newAccessToken = jwtUtil.generateAccessToken(userId);
            RefreshResponseDto refreshResponseDto = new RefreshResponseDto(newAccessToken);

            return APISuccessResponse.of(HttpStatus.OK, refreshResponseDto);
        } catch (ExpiredJwtException e) {
            throw new MokkojiException(FailMessage.UNAUTHORIZED_EXPIRED);
        } catch (JwtException e) {
            throw new MokkojiException(FailMessage.UNAUTHORIZED);
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestHeader("Authorization") String accessToken) {
        accessToken = accessToken.replace("Bearer ", "");
        try {
            Long userId = jwtUtil.getUserIdFromToken(accessToken);

            tokenService.deleteRefreshToken(userId);

            return ResponseEntity.ok("{\"message\": \"로그아웃 성공\"}");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("{\"error\": \"유효하지 않은 토큰입니다.\"}");
        }
    }

}

