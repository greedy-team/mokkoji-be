package com.greedy.mokkoji.api.clubmaster.controller;

import com.greedy.mokkoji.api.auth.controller.argumentResolver.AuthCredential;
import com.greedy.mokkoji.api.clubmaster.dto.request.CreateClubMasterApplicationRequest;
import com.greedy.mokkoji.api.clubmaster.dto.response.GetMyClubMasterApplicationsResponse;
import com.greedy.mokkoji.common.response.APISuccessResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

import java.util.List;

@Tag(name = "Club Master Controller", description = "동아리장 권한 신청 관련 API")
public interface ClubMasterControllerSwagger {

    @Operation(
            summary = "동아리장 권한 요청 신청 API",
            security = {@SecurityRequirement(name = "JWT")}
    )
    @ApiResponse(responseCode = "201", description = "동아리장 신청서 생성 성공")
    ResponseEntity<APISuccessResponse<Void>> createClubMasterApplication(
            @Parameter(hidden = true) AuthCredential authCredential,
            @Parameter(name = "createClubMasterApplicationsRequest", description = "동아리장 신청 요청 본문") CreateClubMasterApplicationRequest request
    );

    @Operation(
            summary = "내 동아리장 권한 요청 현황 조회 API",
            description = "최신 등록순으로 조회",
            security = {@SecurityRequirement(name = "JWT")}
    )
    @ApiResponse(responseCode = "200", description = "조회 성공")
    ResponseEntity<APISuccessResponse<List<GetMyClubMasterApplicationsResponse>>> getMyClubMasterApplications(
            @Parameter(hidden = true) AuthCredential authCredential
    );
}
