package com.greedy.mokkoji.api.feedback.controller;

import com.greedy.mokkoji.api.feedback.dto.request.FeedbackRequest;
import com.greedy.mokkoji.common.response.APISuccessResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

@Tag(name = "Feedback Controller", description = "피드백 관련 API")
public interface FeedbackControllerSwagger {

    @Operation(summary = "피드백 생성 API")
    @ApiResponse(responseCode = "201", description = "피드백 생성 성공")
    ResponseEntity<APISuccessResponse<Void>> createFeedback(
            @Parameter(name = "feedbackRequest", description = "피드백 내용") FeedbackRequest feedbackRequest
    );
}
