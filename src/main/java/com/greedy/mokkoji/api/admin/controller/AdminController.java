package com.greedy.mokkoji.api.admin.controller;

import com.greedy.mokkoji.api.admin.dto.request.RejectClubMasterApplicationRequest;
import com.greedy.mokkoji.api.admin.dto.response.GetClubMasterApplicationsResponse;
import com.greedy.mokkoji.api.admin.service.AdminService;
import com.greedy.mokkoji.api.auth.controller.argumentResolver.AuthCredential;
import com.greedy.mokkoji.api.auth.controller.argumentResolver.Authentication;
import com.greedy.mokkoji.common.response.APISuccessResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminController {

    private final AdminService adminService;

    @GetMapping("club-master-applications")
    public ResponseEntity<APISuccessResponse<GetClubMasterApplicationsResponse>> getClubMasterApplications(
            @Authentication final AuthCredential authCredential,
            @RequestParam(value = "page") final int page,
            @RequestParam(value = "size") final int size
    ) {
        final Pageable pageable = PageRequest.of(page - 1, size);
        return APISuccessResponse.of(
                HttpStatus.OK,
                adminService.getClubMasterApplications(authCredential.authRole(), authCredential.userId(), pageable)
        );
    }

    @PatchMapping("club-master-applications/{applicationId}/approve")
    public ResponseEntity<APISuccessResponse<Void>> approveClubMasterApplication(
            @Authentication final AuthCredential authCredential,
            @PathVariable(name = "applicationId") final Long applicationId
    ) {
        adminService.approveClubMasterApplication(authCredential.authRole(), authCredential.userId(), applicationId);
        return APISuccessResponse.of(HttpStatus.CREATED, null);
    }

    @PatchMapping("club-master-applications/{applicationId}/reject")
    public ResponseEntity<APISuccessResponse<Void>> rejectClubMasterApplication(
            @Authentication final AuthCredential authCredential,
            @PathVariable(name = "applicationId") final Long applicationId,
            @RequestBody RejectClubMasterApplicationRequest request
    ) {
        adminService.rejectClubMasterApplication(authCredential.authRole(), authCredential.userId(), applicationId, request.rejectReason());
        return APISuccessResponse.of(HttpStatus.CREATED, null);
    }
}
