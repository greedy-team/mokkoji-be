package com.greedy.mokkoji.api.clubmaster.controller;

import com.greedy.mokkoji.api.auth.controller.argumentResolver.AuthCredential;
import com.greedy.mokkoji.api.auth.controller.argumentResolver.Authentication;
import com.greedy.mokkoji.api.clubmaster.dto.request.ApplyClubMasterTransferRequest;
import com.greedy.mokkoji.api.clubmaster.dto.request.CreateClubMasterApplicationRequest;
import com.greedy.mokkoji.api.clubmaster.dto.response.GetMyClubMasterApplicationsResponse;
import com.greedy.mokkoji.api.clubmaster.service.ClubMasterService;
import com.greedy.mokkoji.common.response.APISuccessResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("${api.prefix}")
public class ClubMasterController implements ClubMasterControllerSwagger {

    private final ClubMasterService clubMasterApplicationService;

    @PostMapping("/club-master-applications")
    public ResponseEntity<APISuccessResponse<Void>> createClubMasterApplication(
            @Authentication final AuthCredential authCredential,
            @RequestBody final CreateClubMasterApplicationRequest request
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

    @PostMapping("/club-master-transfers")
    public ResponseEntity<APISuccessResponse<Void>> applyClubMasterTransfer(
            @Authentication final AuthCredential authCredential,
            @RequestBody final ApplyClubMasterTransferRequest request
    ) {
        clubMasterApplicationService.applyClubMasterTransfer(
                authCredential.authRole(),
                authCredential.userId(),
                request.clubId(),
                request.nextClubMasterName(),
                request.nextClubMasterEmail()
        );

        return APISuccessResponse.of(HttpStatus.CREATED, null);
    }

    @GetMapping("/club-master-transfer/{uuid}")
    public ResponseEntity<APISuccessResponse<Void>> acceptClubMasterTransfer(
            @Authentication final AuthCredential authCredential,
            @PathVariable("uuid") final String uuid
    ) {
        clubMasterApplicationService.acceptClubMasterTransfer(authCredential.userId(), uuid);

        return APISuccessResponse.of(HttpStatus.OK, null);
    }

}
