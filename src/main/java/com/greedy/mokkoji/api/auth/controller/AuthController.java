package com.greedy.mokkoji.api.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.greedy.mokkoji.api.auth.dto.LoginRequestDto;
import com.greedy.mokkoji.api.auth.dto.LoginResponseDto;
import com.greedy.mokkoji.api.auth.dto.StudentInformationResponseDto;
import com.greedy.mokkoji.api.auth.service.LoginService;
import com.greedy.mokkoji.api.auth.service.TokenService;
import com.greedy.mokkoji.api.jwt.JwtUtil;
import com.greedy.mokkoji.db.user.entity.User;
import com.greedy.mokkoji.db.user.service.UserService;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    public ResponseEntity<String> login(@RequestBody LoginRequestDto request) {
        String id = request.getId();
        String password = request.getPassword();

        try {
            StudentInformationResponseDto response = loginService.getStudentInformation(id, password);
            if (response.name() == null) {
                log.warn("로그인 실패: 잘못된 아이디 또는 비밀번호. studentId={}", id);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("{\"error\": \"잘못된 아이디 또는 비밀번호입니다.\"}");
            }

            User user = userService.findOrCreateUser(response, id);

            String accessToken = jwtUtil.generateAccessToken(user.getId());
            String refreshToken = jwtUtil.generateRefreshToken(user.getId());

            tokenService.saveRefreshToken(user.getId(), refreshToken);

            log.info("Generated Access Token: {}", accessToken);
            log.info("Generated Refresh Token: {}", refreshToken);

            LoginResponseDto tokenResponse = new LoginResponseDto(accessToken, refreshToken);
            return ResponseEntity.ok().body(mapper.writeValueAsString(tokenResponse));

        } catch (Exception e) {
            log.error("서버 내부 오류 발생: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\": \"서버 내부 오류가 발생했습니다. 잠시 후 다시 시도해주세요.\"}");
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<String> refresh(@RequestBody Map<String, String> request) {
        log.info("Access Token 갱신 요청");

        String refreshToken = request.get("refreshToken");
        if (refreshToken == null || refreshToken.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("{\"error\": \"Refresh Token이 제공되지 않았습니다.\"}");
        }

        try {
            // Refresh Token에서 userId 추출
            Long userId = jwtUtil.getUserIdFromToken(refreshToken);

            // Redis에서 저장된 Refresh Token 가져오기
            String storedRefreshToken = tokenService.getRefreshToken(userId);
            if (storedRefreshToken == null || !storedRefreshToken.equals(refreshToken)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("{\"error\": \"유효하지 않은 Refresh Token입니다.\"}");
            }

            // 유저 정보 확인
            Optional<User> user = userService.findUser(userId);
            if (user.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("{\"error\": \"유효하지 않은 사용자입니다.\"}");
            }

            // 새 Access Token 발급
            String newAccessToken = jwtUtil.generateAccessToken(user.get().getId());
            log.info("New Access Token: {}", newAccessToken);

            return ResponseEntity.ok()
                    .body("{\"accessToken\": \"" + newAccessToken + "\"}");
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
    public ResponseEntity<String> logout(@RequestBody Map<String, String> request) {
        String accessToken = request.get("accessToken");
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

