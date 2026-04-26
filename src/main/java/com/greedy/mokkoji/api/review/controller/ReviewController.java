package com.greedy.mokkoji.api.review.controller;

import com.greedy.mokkoji.api.auth.controller.argumentResolver.AuthCredential;
import com.greedy.mokkoji.api.auth.controller.argumentResolver.Authentication;
import com.greedy.mokkoji.api.review.dto.request.ReviewRequest;
import com.greedy.mokkoji.api.review.service.ReviewService;
import com.greedy.mokkoji.common.response.APISuccessResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("${api.prefix}/reports")
public class ReviewController implements ReviewControllerSwagger {

    private final ReviewService reviewService;

    @PostMapping
    public ResponseEntity<APISuccessResponse<Void>> createReview(
            @Authentication final AuthCredential authCredential,
            @RequestBody final ReviewRequest reportRequest
    ) {
        return APISuccessResponse.of(
                HttpStatus.CREATED,
                reviewService.createReport(authCredential.userId(), reportRequest.rating(), reportRequest.content())
        );
    }
}
