package com.greedy.mokkoji.api.clubMasterApplication.controller;

import com.greedy.mokkoji.api.auth.controller.argumentResolver.AuthCredential;
import com.greedy.mokkoji.api.auth.controller.argumentResolver.Authentication;
import com.greedy.mokkoji.api.clubMasterApplication.dto.request.CreateClubMasterApplicationsRequest;
import com.greedy.mokkoji.api.clubMasterApplication.dto.response.GetMyClubMasterApplicationsResponse;
import com.greedy.mokkoji.api.clubMasterApplication.service.ClubMasterApplicationService;
import com.greedy.mokkoji.common.response.APISuccessResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/club-master-applications")
public class ClubMasterApplicationController {

    private final ClubMasterApplicationService clubMasterApplicationService;

    @PostMapping
    public ResponseEntity<APISuccessResponse<Void>> createClubMasterApplication(
            @Authentication final AuthCredential authCredential,
            @RequestBody final CreateClubMasterApplicationsRequest request
    ) {
        clubMasterApplicationService.createClubMasterApplication(
                authCredential.userId(),
                request.universityCode(),
                request.clubId(),
                request.userName()
        );

        return APISuccessResponse.of(HttpStatus.CREATED, null);
    }

    @GetMapping("/me")
    public ResponseEntity<APISuccessResponse<List<GetMyClubMasterApplicationsResponse>>> getMyClubMasterApplications(
            @Authentication final AuthCredential authCredential
    ) {
        return APISuccessResponse.of(
                HttpStatus.OK,
                clubMasterApplicationService.getMyClubMasterApplications(authCredential.userId())
        );
    }
}
