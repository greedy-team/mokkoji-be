package com.greedy.mokkoji.api.test;

import com.greedy.mokkoji.common.response.APISuccessResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("${api.prefix}/test")
public class TestController {

    @GetMapping("/health-check")
    public ResponseEntity<APISuccessResponse<String>> healthCheck() {
        return APISuccessResponse.of(HttpStatus.OK, "OK");
    }
}
