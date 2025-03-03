package com.greedy.mokkoji.common;

import com.greedy.mokkoji.api.external.AppDataS3Client;
import com.greedy.mokkoji.api.jwt.JwtUtil;
import com.greedy.mokkoji.db.user.entity.User;
import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class ControllerTest extends AbstractTest {
    @Autowired
    protected JwtUtil jwtUtil;

    @Value("${api.prefix}")
    protected String prefixUrl;

    @MockitoBean
    protected AppDataS3Client appDataS3Client;

    @LocalServerPort
    private int port;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    protected String authorizationForBearer(final User user) {
        final String accessToken = jwtUtil.generateAccessToken(user.getId());
        return "Bearer " + accessToken;
    }

    protected String authorizationForBearerRefresh(final User user) {
        final String accessToken = jwtUtil.generateAccessToken(user.getId());
        return "Bearer " + accessToken;
    }

    protected <T> T getDataFromResponse(ExtractableResponse<Response> response, Class<T> clazz) {
        return response.jsonPath().getObject("data", clazz);
    }
}
