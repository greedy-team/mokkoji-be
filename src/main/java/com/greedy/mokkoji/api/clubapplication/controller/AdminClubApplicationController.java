package com.greedy.mokkoji.api.clubapplication.controller;

import com.greedy.mokkoji.api.auth.controller.argumentResolver.AuthCredential;
import com.greedy.mokkoji.api.auth.controller.argumentResolver.Authentication;
import com.greedy.mokkoji.api.clubapplication.dto.request.ClubApplicationRejectRequest;
import com.greedy.mokkoji.api.clubapplication.dto.response.AdminClubApplicationsResponse;
import com.greedy.mokkoji.api.clubapplication.service.ClubApplicationService;
import com.greedy.mokkoji.common.response.APISuccessResponse;
import com.greedy.mokkoji.enums.clubApplication.ClubApplicationStatus;
import com.greedy.mokkoji.enums.university.UniversityCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/club-applications")
public class AdminClubApplicationController {

    private final ClubApplicationService clubApplicationService;

    @GetMapping
    public ResponseEntity<APISuccessResponse<AdminClubApplicationsResponse>> getAdminClubApplications(
            @Authentication final AuthCredential authCredential,
            @RequestParam(required = false) final UniversityCode universityCode,
            @RequestParam(required = false) final ClubApplicationStatus status,
            @RequestParam(value = "page") final int page,
            @RequestParam(value = "size") final int size
    ) {
        final Pageable pageable = PageRequest.of(page - 1, size);
        return APISuccessResponse.of(
                HttpStatus.OK,
                clubApplicationService.getAdminClubApplications(authCredential.authRole(), authCredential.userId(), universityCode, status, pageable)
        );
    }

    @PatchMapping("/{applicationId}/approve")
    public ResponseEntity<APISuccessResponse<Void>> approveClubApplication(
            @Authentication final AuthCredential authCredential,
            @PathVariable final Long applicationId
    ) {
        clubApplicationService.approveClubApplication(authCredential.authRole(), authCredential.userId(), applicationId);
        return APISuccessResponse.of(HttpStatus.OK, null);
    }

    @PatchMapping("/{applicationId}/reject")
    public ResponseEntity<APISuccessResponse<Void>> rejectClubApplication(
            @Authentication final AuthCredential authCredential,
            @PathVariable final Long applicationId,
            @RequestBody final ClubApplicationRejectRequest request
    ) {
        clubApplicationService.rejectClubApplication(authCredential.authRole(), authCredential.userId(), applicationId, request.rejectReason());
        return APISuccessResponse.of(HttpStatus.OK, null);
    }
}
