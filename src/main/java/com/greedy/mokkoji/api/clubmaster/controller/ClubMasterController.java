package com.greedy.mokkoji.api.clubmaster.controller;

import com.greedy.mokkoji.api.auth.controller.argumentResolver.AuthCredential;
import com.greedy.mokkoji.api.auth.controller.argumentResolver.Authentication;
import com.greedy.mokkoji.api.clubmaster.dto.request.CreateClubMasterApplicationsRequest;
import com.greedy.mokkoji.api.clubmaster.dto.response.GetMyClubMasterApplicationsResponse;
import com.greedy.mokkoji.api.clubmaster.service.ClubMasterService;
import com.greedy.mokkoji.common.response.APISuccessResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ClubMasterController implements ClubMasterControllerSwagger {

    private final ClubMasterService clubMasterApplicationService;

    @PostMapping("${api.prefix}/club-master-applications")
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

    @GetMapping("/club-master-applications/me")
    public ResponseEntity<APISuccessResponse<List<GetMyClubMasterApplicationsResponse>>> getMyClubMasterApplications(
            @Authentication final AuthCredential authCredential
    ) {
        return APISuccessResponse.of(
                HttpStatus.OK,
                clubMasterApplicationService.getMyClubMasterApplications(authCredential.userId())
        );
    }
}
