package com.greedy.mokkoji.api.clubapplication.controller;

import com.greedy.mokkoji.api.auth.controller.argumentResolver.AuthCredential;
import com.greedy.mokkoji.api.auth.controller.argumentResolver.Authentication;
import com.greedy.mokkoji.api.clubapplication.dto.request.ClubApplicationRejectRequest;
import com.greedy.mokkoji.api.clubapplication.dto.response.AdminClubApplicationsResponse;
import com.greedy.mokkoji.api.clubapplication.service.AdminClubApplicationService;
import com.greedy.mokkoji.common.response.APISuccessResponse;
import com.greedy.mokkoji.enums.application.ApplicationStatus;
import com.greedy.mokkoji.enums.university.UniversityCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("${api.prefix}/admin/club-applications")
public class AdminClubApplicationController implements AdminClubApplicationControllerSwagger {

    private final AdminClubApplicationService adminClubApplicationService;

    @GetMapping
    public ResponseEntity<APISuccessResponse<AdminClubApplicationsResponse>> getAdminClubApplications(
            @Authentication final AuthCredential authCredential,
            @RequestParam(required = false) final UniversityCode universityCode,
            @RequestParam(required = false) final ApplicationStatus status,
            @RequestParam(value = "page") final int page,
            @RequestParam(value = "size") final int size
    ) {
        final Pageable pageable = PageRequest.of(page - 1, size);
        return APISuccessResponse.of(
                HttpStatus.OK,
                adminClubApplicationService.getAdminClubApplications(authCredential.authRole(), authCredential.accountId(), universityCode, status, pageable)
        );
    }

    @PatchMapping("/{applicationId}/approve")
    public ResponseEntity<APISuccessResponse<Void>> approveClubApplication(
            @Authentication final AuthCredential authCredential,
            @PathVariable final Long applicationId
    ) {
        adminClubApplicationService.approveClubApplication(authCredential.authRole(), authCredential.accountId(), applicationId);
        return APISuccessResponse.of(HttpStatus.OK, null);
    }

    @PatchMapping("/{applicationId}/reject")
    public ResponseEntity<APISuccessResponse<Void>> rejectClubApplication(
            @Authentication final AuthCredential authCredential,
            @PathVariable final Long applicationId,
            @RequestBody final ClubApplicationRejectRequest request
    ) {
        adminClubApplicationService.rejectClubApplication(authCredential.authRole(), authCredential.accountId(), applicationId, request.rejectReason());
        return APISuccessResponse.of(HttpStatus.OK, null);
    }
}
