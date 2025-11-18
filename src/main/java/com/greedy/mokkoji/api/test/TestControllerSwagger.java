package com.greedy.mokkoji.api.test;

import com.greedy.mokkoji.common.response.APISuccessResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

@Tag(name = "Test Controller", description = "서버 상태 확인용 API")
public interface TestControllerSwagger {

    @Operation(
            summary = "서버 상태 체크 API",
            description = "서버가 정상적으로 동작 중인지 확인합니다."
    )
    @ApiResponse(responseCode = "200", description = "서버 정상 동작")
    ResponseEntity<APISuccessResponse<String>> healthCheck();
}
