package com.greedy.mokkoji.api.clubmaster.controller;

import com.greedy.mokkoji.api.admin.dto.request.RejectClubMasterApplicationRequest;
import com.greedy.mokkoji.api.admin.dto.response.GetClubMasterApplicationsResponse;
import com.greedy.mokkoji.api.auth.controller.argumentResolver.AuthCredential;
import com.greedy.mokkoji.api.auth.controller.argumentResolver.Authentication;
import com.greedy.mokkoji.api.clubmaster.service.AdminClubMasterService;
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
@RequestMapping("${api.prefix}/admin/club-master-applications")
public class AdminClubMasterController implements AdminClubMasterSwagger {
    private final AdminClubMasterService adminClubMasterService;

    @GetMapping
    public ResponseEntity<APISuccessResponse<GetClubMasterApplicationsResponse>> getClubMasterApplications(
            @Authentication final AuthCredential authCredential,
            @RequestParam(required = false) final UniversityCode universityCode,
            @RequestParam(required = false) final ApplicationStatus status,
            @RequestParam(value = "page") final int page,
            @RequestParam(value = "size") final int size) {
        final Pageable pageable = PageRequest.of(page - 1, size);
        return APISuccessResponse.of(
                HttpStatus.OK,
                adminClubMasterService.getClubMasterApplications(authCredential.authRole(), authCredential.accountId(), universityCode, status, pageable)
        );
    }

    @PatchMapping("/{applicationId}/approve")
    public ResponseEntity<APISuccessResponse<Void>> approveClubMasterApplication(
            @Authentication final AuthCredential authCredential,
            @PathVariable(name = "applicationId") final Long applicationId
    ) {
        adminClubMasterService.approveClubMasterApplication(authCredential.authRole(), authCredential.accountId(), applicationId);
        return APISuccessResponse.of(HttpStatus.CREATED, null);
    }

    @PatchMapping("/{applicationId}/reject")
    public ResponseEntity<APISuccessResponse<Void>> rejectClubMasterApplication(
            @Authentication final AuthCredential authCredential,
            @PathVariable(name = "applicationId") final Long applicationId,
            @RequestBody RejectClubMasterApplicationRequest request
    ) {
        adminClubMasterService.rejectClubMasterApplication(authCredential.authRole(), authCredential.accountId(), applicationId, request.rejectReason());
        return APISuccessResponse.of(HttpStatus.CREATED, null);
    }
}
