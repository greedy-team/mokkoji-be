package com.greedy.mokkoji.api.admin.controller;


import com.greedy.mokkoji.api.admin.dto.request.RejectClubMasterApplicationRequest;
import com.greedy.mokkoji.api.admin.dto.response.GetClubMasterApplicationsResponse;
import com.greedy.mokkoji.api.auth.controller.argumentResolver.AuthCredential;
import com.greedy.mokkoji.common.response.APISuccessResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

@Tag(name = "Admin Controller", description = "관리자 관련 API")
public interface AdminControllerSwagger {

    @Operation(
            summary = "동아리장 권한 요청 조회 API",
            description = "오래된 등록순으로 조회",
            security = {@SecurityRequirement(name = "JWT")}
    )
    @ApiResponse(responseCode = "200", description = "조회 성공")
    ResponseEntity<APISuccessResponse<GetClubMasterApplicationsResponse>> getClubMasterApplications(
            @Parameter(hidden = true) AuthCredential authCredential,
            @Parameter(name = "page", description = "페이지 번호 (1부터 시작)", example = "1") int page,
            @Parameter(name = "size", description = "페이지 크기", example = "10") int size
    );

    @Operation(
            summary = "동아리장 권한 요청 승인 API",
            security = {@SecurityRequirement(name = "JWT")}
    )
    @ApiResponse(responseCode = "201", description = "승인 성공")
    ResponseEntity<APISuccessResponse<Void>> approveClubMasterApplication(
            @Parameter(hidden = true) AuthCredential authCredential,
            @Parameter(name = "applicationId", description = "동아리장 신청서 ID") Long applicationId
    );

    @Operation(
            summary = "동아리장 권한 요청 거절 API",
            security = {@SecurityRequirement(name = "JWT")}
    )
    @ApiResponse(responseCode = "201", description = "거절 성공")
    ResponseEntity<APISuccessResponse<Void>> rejectClubMasterApplication(
            @Parameter(hidden = true) AuthCredential authCredential,
            @Parameter(name = "applicationId", description = "동아리장 신청서 ID") Long applicationId,
            @Parameter(name = "rejectClubMasterApplicationRequest", description = "동아리장 거절 요청 본문") RejectClubMasterApplicationRequest request
    );
}
