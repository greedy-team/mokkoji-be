package com.greedy.mokkoji.api.clubapplication.controller;

import com.greedy.mokkoji.api.auth.controller.argumentResolver.AuthCredential;
import com.greedy.mokkoji.api.clubapplication.dto.request.ClubApplicationRejectRequest;
import com.greedy.mokkoji.api.clubapplication.dto.response.AdminClubApplicationsResponse;
import com.greedy.mokkoji.common.response.APISuccessResponse;
import com.greedy.mokkoji.enums.application.ApplicationStatus;
import com.greedy.mokkoji.enums.university.UniversityCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

@Tag(name = "Admin Club Application Controller", description = "관리자용 동아리 생성 신청 관련 API")
public interface AdminClubApplicationControllerSwagger {

    @Operation(
            summary = "관리자용 동아리 생성 신청 목록 조회 API",
            description = "페이지 번호(`page`)는 1부터 시작",
            security = @SecurityRequirement(name = "JWT")
    )
    @ApiResponse(responseCode = "200", description = "조회 성공")
    ResponseEntity<APISuccessResponse<AdminClubApplicationsResponse>> getAdminClubApplications(
            @Parameter(hidden = true) AuthCredential authCredential,
            @Parameter(name = "universityCode", description = "대학교 코드") UniversityCode universityCode,
            @Parameter(name = "status", description = "신청 상태") ApplicationStatus status,
            @Parameter(name = "page", description = "페이지 번호") int page,
            @Parameter(name = "size", description = "페이지 크기") int size
    );

    @Operation(
            summary = "동아리 생성 신청 승인 API",
            security = @SecurityRequirement(name = "JWT")
    )
    @ApiResponse(responseCode = "200", description = "승인 성공")
    ResponseEntity<APISuccessResponse<Void>> approveClubApplication(
            @Parameter(hidden = true) AuthCredential authCredential,
            @Parameter(name = "applicationId", description = "신청 ID", in = ParameterIn.PATH) Long applicationId
    );

    @Operation(
            summary = "동아리 생성 신청 거절 API",
            security = @SecurityRequirement(name = "JWT")
    )
    @ApiResponse(responseCode = "200", description = "거절 성공")
    ResponseEntity<APISuccessResponse<Void>> rejectClubApplication(
            @Parameter(hidden = true) AuthCredential authCredential,
            @Parameter(name = "applicationId", description = "신청 ID", in = ParameterIn.PATH) Long applicationId,
            @Parameter(name = "request", description = "거절 사유") ClubApplicationRejectRequest request
    );
}
