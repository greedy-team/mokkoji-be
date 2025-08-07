package com.greedy.mokkoji.api.recruitment.controller;

import com.greedy.mokkoji.api.auth.controller.argumentResolver.AuthCredential;
import com.greedy.mokkoji.api.auth.controller.argumentResolver.Authentication;
import com.greedy.mokkoji.api.recruitment.dto.request.CreateRecruitmentRequest;
import com.greedy.mokkoji.api.recruitment.dto.request.UpdateRecruitmentRequest;
import com.greedy.mokkoji.api.recruitment.dto.response.AllRecruitment.AllRecruitmentResponse;
import com.greedy.mokkoji.api.recruitment.dto.response.allRecruitmentOfClub.AllRecruitmentOfClubResponse;
import com.greedy.mokkoji.api.recruitment.dto.response.createRecruitment.CreateRecruitmentResponse;
import com.greedy.mokkoji.api.recruitment.dto.response.deleteRecruitment.DeleteRecruitmentResponse;
import com.greedy.mokkoji.api.recruitment.dto.response.specificRecruitment.SpecificRecruitmentResponse;
import com.greedy.mokkoji.api.recruitment.dto.response.updateRecruitment.UpdateRecruitmentResponse;
import com.greedy.mokkoji.api.recruitment.service.RecruitmentService;
import com.greedy.mokkoji.common.response.APISuccessResponse;
import com.greedy.mokkoji.enums.club.ClubAffiliation;
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
    public ResponseEntity<APISuccessResponse<CreateRecruitmentResponse>> createRecruitment(
            @Authentication final AuthCredential authCredential,
            @PathVariable("clubId") final Long clubId,
            @RequestBody CreateRecruitmentRequest request
    ) {
        return APISuccessResponse.of(
                HttpStatus.OK,
                recruitmentService.createRecruitment(
                        authCredential.userId(),
                        clubId,
                        request.title(),
                        request.content(),
                        request.recruitStart(),
                        request.recruitEnd(),
                        request.images(),
                        request.recruitForm()
                )
        );
    }

    @PatchMapping("/{recruitmentId}")
    public ResponseEntity<APISuccessResponse<UpdateRecruitmentResponse>> updateRecruitment(
            @Authentication final AuthCredential authCredential,
            @PathVariable("recruitmentId") final Long recruitmentId,
            @RequestBody UpdateRecruitmentRequest request
    ) {
        UpdateRecruitmentResponse response = recruitmentService.updateRecruitment(
                authCredential.userId(),
                recruitmentId,
                request.title(),
                request.content(),
                request.recruitStart(),
                request.recruitEnd(),
                request.images(),
                request.recruitForm()
        );
        return APISuccessResponse.of(HttpStatus.OK, response);
    }

    @DeleteMapping("/{recruitmentId}")
    public ResponseEntity<APISuccessResponse<DeleteRecruitmentResponse>> deleteRecruitment(
            @Authentication final AuthCredential authCredential,
            @PathVariable("recruitmentId") final Long recruitmentId
    ) {
        DeleteRecruitmentResponse response = recruitmentService.deleteRecruitment(authCredential.userId(), recruitmentId);
        return APISuccessResponse.of(HttpStatus.OK, response);
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
            @Authentication final AuthCredential authCredential,
            @PathVariable("recruitmentId") final Long recruitmentId
    ) {
        return APISuccessResponse.of(
                HttpStatus.OK,
                recruitmentService.getSpecificRecruitment(authCredential.userId(), recruitmentId)
        );
    }

    @GetMapping
    public ResponseEntity<APISuccessResponse<AllRecruitmentResponse>> getAllRecruitment(
            @Authentication final AuthCredential authCredential,
            @RequestParam(value = "affiliation", required = false) final ClubAffiliation affiliation,
            @RequestParam(value = "page") final int page,
            @RequestParam(value = "size") final int size
    ) {
        final Pageable pageable = PageRequest.of(page - 1, size);
        return APISuccessResponse.of(
                HttpStatus.OK,
                recruitmentService.getAllRecruitment(authCredential.userId(), affiliation, pageable)
        );
    }
}
