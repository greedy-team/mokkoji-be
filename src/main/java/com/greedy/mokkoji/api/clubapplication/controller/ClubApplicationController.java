package com.greedy.mokkoji.api.clubapplication.controller;

import com.greedy.mokkoji.api.auth.controller.argumentResolver.AuthCredential;
import com.greedy.mokkoji.api.auth.controller.argumentResolver.Authentication;
import com.greedy.mokkoji.api.clubapplication.dto.request.ClubApplicationCreateRequest;
import com.greedy.mokkoji.api.clubapplication.service.ClubApplicationService;
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
@RequestMapping("/club-applications")
public class ClubApplicationController {

    private final ClubApplicationService clubApplicationService;

    @PostMapping
    public ResponseEntity<APISuccessResponse<Void>> createClubApplication(
            @RequestBody final ClubApplicationCreateRequest request,
            @Authentication final AuthCredential authCredential
    ) {
        clubApplicationService.createClubApplication(authCredential.userId(), request);
        return APISuccessResponse.of(HttpStatus.CREATED, null);
    }
}
