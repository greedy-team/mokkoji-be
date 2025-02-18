package com.greedy.mokkoji.api.login.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.greedy.mokkoji.api.login.JwtUtil;
import com.greedy.mokkoji.api.login.dto.LoginRequestDto;
import com.greedy.mokkoji.api.login.dto.StudentInformationResponseDto;
import com.greedy.mokkoji.api.login.service.LoginService;
import com.greedy.mokkoji.db.user.entity.User;
import com.greedy.mokkoji.db.user.repository.UserRepository;
import com.greedy.mokkoji.db.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/auth")
public class LoginController {
    private final UserService userService;
    private final LoginService loginService;
    private final ObjectMapper mapper;
    private final JwtUtil jwtUtil;

    @Autowired
    UserRepository userRepository;

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody LoginRequestDto request) {
        String id = request.getId();
        String password = request.getPassword();
        MultiValueMap<String, String> multiValueMap = new LinkedMultiValueMap<>();
        multiValueMap.add("id", id);
        multiValueMap.add("password", password);

        try {
            StudentInformationResponseDto response = loginService.getStudentInformation(id, password);

            // 로그인 실패 시 (이전 코드에서 null 체크 오류 수정)
            if (response.getName() == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("{\"error\": \"Invalid credentials\"}");
            }

            String name = response.getName();
            String department = response.getDepartment();
            String grade = response.getGrade();
            String studentId = id;

            Optional<User> findUser = userRepository.findByStudentId(studentId);


            if (findUser.isEmpty()) {
                log.info("User not found. Saving new user from response.");
                userService.saveUser(response, studentId);
                findUser = userRepository.findByStudentId(studentId);


                if (findUser.isEmpty()) {
                    log.error("Failed to retrieve user after saving. Aborting operation.");
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("{\"error\": \"Failed to retrieve user\"}");
                }
                log.info("User created and retrieved: {}", findUser.get());
            }

            // JWT 토큰 생성
            String token = jwtUtil.generateToken(studentId);
            log.info("Generated Token: {}", token);

            // User 엔티티의 정보와 함께 JSON 응답 생성
            Map<String, Object> userInfoMap = new HashMap<>();
            userInfoMap.put("token", token);
            userInfoMap.put("studentId", findUser.get().getStudentId());
            userInfoMap.put("name", name);
            userInfoMap.put("department", department);
            userInfoMap.put("grade", grade);

            // JSON 변환
            String userInfoJson = mapper.writeValueAsString(userInfoMap);
            return ResponseEntity.ok().body(userInfoJson);

        } catch (Exception e) {
            log.error("An error occurred during login: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("{\"error\": \"Internal server error\"}");
        }
    }

}
