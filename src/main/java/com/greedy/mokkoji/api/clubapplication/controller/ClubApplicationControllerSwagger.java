package com.greedy.mokkoji.api.clubapplication.controller;

import com.greedy.mokkoji.api.auth.controller.argumentResolver.AuthCredential;
import com.greedy.mokkoji.api.clubapplication.dto.request.ClubApplicationCreateRequest;
import com.greedy.mokkoji.api.clubapplication.dto.response.ClubApplicationsResponse;
import com.greedy.mokkoji.common.response.APISuccessResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

@Tag(name = "Club Application Controller", description = "동아리 생성 신청 관련 API")
public interface ClubApplicationControllerSwagger {

    @Operation(
            summary = "동아리 생성 신청 API",
            security = @SecurityRequirement(name = "JWT")
    )
    @ApiResponse(responseCode = "201", description = "신청 성공")
    ResponseEntity<APISuccessResponse<Void>> createClubApplication(
            @Parameter(name = "request", description = "동아리 생성 신청 요청 본문") ClubApplicationCreateRequest request,
            @Parameter(hidden = true) AuthCredential authCredential
    );

    @Operation(
            summary = "내 동아리 생성 신청 목록 조회 API",
            security = @SecurityRequirement(name = "JWT")
    )
    @ApiResponse(responseCode = "200", description = "조회 성공")
    ResponseEntity<APISuccessResponse<ClubApplicationsResponse>> getMyClubApplications(
            @Parameter(hidden = true) AuthCredential authCredential
    );
}
