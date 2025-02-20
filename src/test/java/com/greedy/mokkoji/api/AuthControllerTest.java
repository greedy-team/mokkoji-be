package com.greedy.mokkoji.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.greedy.mokkoji.api.auth.controller.AuthController;
import com.greedy.mokkoji.api.auth.dto.LoginRequestDto;
import com.greedy.mokkoji.api.auth.service.LoginService;
import com.greedy.mokkoji.api.jwt.JwtUtil;
import com.greedy.mokkoji.db.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.mock;

@WebMvcTest(AuthController.class)
@Import(AuthControllerTest.TestConfig.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserService userService;

    @Autowired
    private AuthController authController;

    @Autowired
    private LoginService loginService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${id}")
    private String id;

    @Value("${password}")
    private String password;

    @Test
    void 실제_로그인_성공시_200_응답코드가_반환되는지_확인() throws Exception {
        //given
        LoginRequestDto requestDto = new LoginRequestDto(id, password);

        //when
        ResponseEntity<String> responseEntity = authController.login(requestDto);

        //then
        assertThat(responseEntity.getStatusCodeValue()).isEqualTo(200);
    }

    @TestConfiguration
    static class TestConfig {
        @Bean
        public UserService userService() {
            return mock(UserService.class);
        }

        @Bean
        public LoginService loginService() {
            return mock(LoginService.class);
        }

        @Bean
        public JwtUtil jwtUtil() {
            return mock(JwtUtil.class);
        }
    }

//    @Test
//    void 로그인_성공시_유저정보가_정상적으로_조회되는지_확인() throws Exception {
//        // given
//        LoginRequestDto requestDto = new LoginRequestDto(id, password);
//
//        StudentInformationResponseDto responseDto = new StudentInformationResponseDto("신혜빈", "컴퓨터공학과", "3");
//        User mockUser = new User(id, "신혜빈", "컴퓨터공학과", "3");
//        ReflectionTestUtils.setField(mockUser, "id", 1L);
//
//        when(loginService.getStudentInformation(anyString(), anyString())).thenReturn(responseDto);
//        when(userService.findOrCreateUser(any(), anyString())).thenReturn(mockUser);
//
//        // when & then
//        mockMvc.perform(post("/auth/login")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(requestDto)))
//                .andExpect(status().isOk());
//    }


//    @Test
//    void 로그인_성공시_토큰이_발급되는지_확인() throws Exception {
//        // given
//        String studentId = id;
//        String password = password;
//        LoginRequestDto requestDto = new LoginRequestDto(studentId, password);
//
//        StudentInformationResponseDto responseDto = new StudentInformationResponseDto("신혜빈", "컴퓨터공학과", "3");
//        User mockUser = new User(studentId, "신혜빈", "컴퓨터공학과", "3");
//        ReflectionTestUtils.setField(mockUser, "id", 1L);
//
//        when(loginService.getStudentInformation(anyString(), anyString())).thenReturn(responseDto);
//        when(userService.findOrCreateUser(any(), anyString())).thenReturn(mockUser);
//        when(jwtUtil.generateAccessToken(1L)).thenReturn("accessToken");
//        when(jwtUtil.generateRefreshToken(1L)).thenReturn("refreshToken");
//
//        // when & then
//        mockMvc.perform(post("/auth/login")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(requestDto)))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.accessToken").value("accessToken"))
//                .andExpect(jsonPath("$.refreshToken").value("refreshToken"));
//    }
}
