package com.greedy.mokkoji.api.test;

import com.greedy.mokkoji.common.response.APISuccessResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("${api.prefix}/test")
public class TestController {

    @Operation(summary = "서버 상태 체크 API")
    @ApiResponse(responseCode = "200", description = "서버 정상 동작")
    @GetMapping("/health-check")
    public ResponseEntity<APISuccessResponse<String>> healthCheck() {
        return APISuccessResponse.of(HttpStatus.OK, "OK");
    }
}
