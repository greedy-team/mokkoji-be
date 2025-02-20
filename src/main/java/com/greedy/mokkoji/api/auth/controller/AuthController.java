package com.greedy.mokkoji.api.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.greedy.mokkoji.api.auth.dto.LoginRequestDto;
import com.greedy.mokkoji.api.auth.dto.LoginResponseDto;
import com.greedy.mokkoji.api.auth.dto.StudentInformationResponseDto;
import com.greedy.mokkoji.api.auth.service.LoginService;
import com.greedy.mokkoji.api.jwt.JwtUtil;
import com.greedy.mokkoji.db.user.entity.User;
import com.greedy.mokkoji.db.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/auth")
public class AuthController {
    private final UserService userService;
    private final LoginService loginService;
    private final ObjectMapper mapper;
    private final JwtUtil jwtUtil;

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody LoginRequestDto request) {
        String id = request.getId();
        String password = request.getPassword();

        try {
            StudentInformationResponseDto response = loginService.getStudentInformation(id, password);

            if (response.getName() == null) {
                log.warn("로그인 실패: 잘못된 아이디 또는 비밀번호. studentId={}", id);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("{\"error\": \"잘못된 아이디 또는 비밀번호입니다.\"}");
            }
            String studentId = id;
            User user = userService.findOrCreateUser(response, studentId);

            String accessToken = jwtUtil.generateAccessToken(user.getId());
            String refreshToken = jwtUtil.generateRefreshToken(user.getId());
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
}

