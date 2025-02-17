package com.greedy.mokkoji.api.login.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.greedy.mokkoji.api.login.JwtUtil;
import com.greedy.mokkoji.api.login.dto.LoginRequestDto;
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
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@RestController
public class LoginController {
    private static final String apiUrl = "https://auth.imsejong.com/auth?method=ClassicSession";
    private final RestTemplate restTemplate;
    private final UserService userService;
    private final ObjectMapper mapper;
    private final JwtUtil jwtUtil;

    @Autowired
    UserRepository userRepository;

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody LoginRequestDto request) {
        String id = request.getId();
        String pw = request.getPw();
        MultiValueMap<String, String> multiValueMap = new LinkedMultiValueMap<>();
        multiValueMap.add("id", id);
        multiValueMap.add("pw", pw);
        log.info("multivaluemap = {}", multiValueMap);

        try {
            ResponseEntity<String> responseEntity = restTemplate.postForEntity(apiUrl, request, String.class);
            if (responseEntity.getStatusCode() == HttpStatus.OK) {
                String responseBody = responseEntity.getBody();
                JsonNode jsonNode = mapper.readTree(responseBody);
                boolean isAuthenticated = jsonNode.get("result").get("is_auth").asBoolean();

                if (isAuthenticated) {
                    JsonNode bodyNode = jsonNode.get("result").get("body");
                    String name = bodyNode.get("name").asText();
                    String department = bodyNode.get("major").asText();
                    String grade = bodyNode.get("grade").asText();

                    String studentId = id;
                    Optional<User> findUser = userRepository.findById(Long.parseLong(studentId));

                    // Log to check if findUser has been retrieved
                    if (findUser != null) {
                        log.info("User found: {}", findUser);
                    } else {
                        log.info("User not found. Saving new user from response.");
                        userService.saveUser(responseBody, studentId);
                        findUser = userRepository.findById(Long.parseLong(studentId));
                        if (findUser == null) {
                            log.error("Failed to retrieve user after saving. Aborting operation.");
                            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
                        }
                        log.info("User created and retrieved: {}", findUser);
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

                    String userInfoJson = mapper.writeValueAsString(userInfoMap);
                    return ResponseEntity.ok(userInfoJson);
                } else {
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
                }
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
        } catch (Exception e) {
            log.error("An error occurred during login: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
