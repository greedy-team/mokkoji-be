package com.greedy.mokkoji.api.review.controller;

import com.greedy.mokkoji.api.auth.controller.argumentResolver.AuthCredential;
import com.greedy.mokkoji.api.review.dto.request.ReviewRequest;
import com.greedy.mokkoji.common.response.APISuccessResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

@Tag(name = "Review Controller", description = "피드백 관련 API")
public interface ReviewControllerSwagger {

    @Operation(
            summary = "피드백 생성 API",
            security = {@SecurityRequirement(name = "JWT")}
    )
    @ApiResponse(responseCode = "201", description = "피드백 생성 성공")
    ResponseEntity<APISuccessResponse<Void>> createReview(
            @Parameter(hidden = true) AuthCredential authCredential,
            @Parameter(name = "reviewRequest", description = "피드백 내용") ReviewRequest reportRequest
    );
}
