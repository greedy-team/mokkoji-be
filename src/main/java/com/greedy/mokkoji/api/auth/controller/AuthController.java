package com.greedy.mokkoji.api.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.greedy.mokkoji.api.auth.dto.LoginRequestDto;
import com.greedy.mokkoji.api.auth.dto.LoginResponseDto;
import com.greedy.mokkoji.api.auth.dto.RefreshResponseDto;
import com.greedy.mokkoji.api.auth.dto.StudentInformationResponseDto;
import com.greedy.mokkoji.api.auth.service.LoginService;
import com.greedy.mokkoji.api.auth.service.TokenService;
import com.greedy.mokkoji.api.jwt.JwtUtil;
import com.greedy.mokkoji.common.response.APISuccessResponse;
import com.greedy.mokkoji.db.user.entity.User;
import com.greedy.mokkoji.db.user.service.UserService;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("{\"error\": \"Refresh Token이 제공되지 않았습니다.\"}");
        }

        try {
            Long userId = jwtUtil.getUserIdFromToken(refreshToken);

            String storedRefreshToken = tokenService.getRefreshToken(userId);
            if (storedRefreshToken == null || !storedRefreshToken.equals(refreshToken)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("{\"error\": \"유효하지 않은 Refresh Token입니다.\"}");
            }

            String newAccessToken = jwtUtil.generateAccessToken(userId);
            RefreshResponseDto refreshResponseDto = new RefreshResponseDto(newAccessToken);

            return APISuccessResponse.of(HttpStatus.OK, refreshResponseDto);
        } catch (ExpiredJwtException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("{\"error\": \"Refresh Token이 만료되었습니다. 다시 로그인해주세요.\"}");
        } catch (JwtException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("{\"error\": \"유효하지 않은 Refresh Token입니다.\"}");
        } catch (Exception e) {
            log.error("서버 내부 오류 발생: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\": \"서버 내부 오류가 발생했습니다.\"}");
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

