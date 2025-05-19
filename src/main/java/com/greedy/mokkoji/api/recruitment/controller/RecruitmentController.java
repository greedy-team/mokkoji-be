package com.greedy.mokkoji.api.recruitment.controller;

import com.greedy.mokkoji.api.auth.controller.argumentResolver.AuthCredential;
import com.greedy.mokkoji.api.auth.controller.argumentResolver.Authentication;
import com.greedy.mokkoji.api.recruitment.dto.request.RecruitmentCreateRequest;
import com.greedy.mokkoji.api.recruitment.dto.response.AllRecruitmentOfClubResponse;
import com.greedy.mokkoji.api.recruitment.dto.response.AllRecruitmentResponse;
import com.greedy.mokkoji.api.recruitment.dto.response.RecruitmentCreateResponse;
import com.greedy.mokkoji.api.recruitment.dto.response.SpecificRecruitmentResponse;
import com.greedy.mokkoji.api.recruitment.service.RecruitmentService;
import com.greedy.mokkoji.common.response.APISuccessResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("${api.prefix}/recruitments")
public class RecruitmentController {
    private final RecruitmentService recruitmentService;

    @PostMapping("/{clubId}")
    public ResponseEntity<APISuccessResponse<RecruitmentCreateResponse>> createRecruitment(
            @Authentication final AuthCredential authCredential,
            @PathVariable("clubId") final Long clubId,
            @RequestBody RecruitmentCreateRequest recruitmentCreateRequest
    ) {
        return APISuccessResponse.of(
                HttpStatus.OK,
                recruitmentService.createRecruitment(authCredential.userId(), clubId, recruitmentCreateRequest)
        );
    }

    @GetMapping("/club/{clubId}")
    public ResponseEntity<APISuccessResponse<AllRecruitmentOfClubResponse>> getAllRecruitmentOfClub(
            @PathVariable("clubId") final Long clubId
    ) {
        return APISuccessResponse.of(
                HttpStatus.OK,
                recruitmentService.getAllRecruitmentOfClub(clubId)
        );
    }

    @GetMapping("/{recruitmentId}")
    public ResponseEntity<APISuccessResponse<SpecificRecruitmentResponse>> getSpecificRecruitment(
            @PathVariable("recruitmentId") final Long recruitmentId
    ) {
        return APISuccessResponse.of(
                HttpStatus.OK,
                recruitmentService.getSpecificRecruitment(recruitmentId)
        );
    }

    @GetMapping
    public ResponseEntity<APISuccessResponse<AllRecruitmentResponse>> getAllRecruitment(
            @Authentication final AuthCredential authCredential,
            @RequestParam(value = "page") final int page,
            @RequestParam(value = "size") final int size
    ) {
        final Pageable pageable = PageRequest.of(page - 1, size);
        return APISuccessResponse.of(
                HttpStatus.OK,
                recruitmentService.getAllRecruitment(authCredential.userId(), pageable)
        );
    }
}
