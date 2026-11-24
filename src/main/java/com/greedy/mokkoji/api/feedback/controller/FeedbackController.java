package com.greedy.mokkoji.api.feedback.controller;

import com.greedy.mokkoji.api.feedback.dto.request.FeedbackRequest;
import com.greedy.mokkoji.api.feedback.service.FeedbackService;
import com.greedy.mokkoji.common.response.APISuccessResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("${api.prefix}/feedbacks")
public class FeedbackController implements FeedbackControllerSwagger {

    private final FeedbackService feedbackService;

    @PostMapping
    public ResponseEntity<APISuccessResponse<Void>> createFeedback(
            @RequestBody final FeedbackRequest feedbackRequest,
            final HttpServletRequest request
    ) {
        return APISuccessResponse.of(
                HttpStatus.CREATED,
                feedbackService.createFeedback(
                        feedbackRequest.rating(),
                        feedbackRequest.content(),
                        feedbackRequest.school(),
                        getClientIp(request)
                )
        );
    }

    private String getClientIp(final HttpServletRequest request) {
        final String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
