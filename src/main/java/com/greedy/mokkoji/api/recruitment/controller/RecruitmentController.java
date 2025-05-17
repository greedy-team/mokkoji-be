package com.greedy.mokkoji.api.recruitment.controller;

import com.greedy.mokkoji.api.auth.controller.argumentResolver.AuthCredential;
import com.greedy.mokkoji.api.auth.controller.argumentResolver.Authentication;
import com.greedy.mokkoji.api.recruitment.dto.RecruitmentCreateRequest;
import com.greedy.mokkoji.api.recruitment.dto.RecruitmentCreateResponse;
import com.greedy.mokkoji.api.recruitment.service.RecruitmentService;
import com.greedy.mokkoji.common.response.APISuccessResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("${api.prefix}/recruiments")
public class RecruitmentController {
    private final RecruitmentService recruitmentService;

    @PostMapping("/{clubId}")
    public ResponseEntity<APISuccessResponse<RecruitmentCreateResponse>> createRecruitment(
            @Authentication final AuthCredential authCredential,
            @PathVariable("clubId") final Long clubId,
            @RequestBody RecruitmentCreateRequest recruitmentCreateRequest
            ){
        return APISuccessResponse.of(
                HttpStatus.OK,
                recruitmentService.createResponse(authCredential.userId(),clubId,recruitmentCreateRequest)
        );
    }
}
