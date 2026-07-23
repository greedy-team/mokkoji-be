package com.greedy.mokkoji.api.admin.controller;

import com.greedy.mokkoji.api.admin.dto.request.AdminLoginRequest;
import com.greedy.mokkoji.api.admin.dto.response.AdminLoginResponse;
import com.greedy.mokkoji.api.admin.dto.response.AdminInfoResponse;
import com.greedy.mokkoji.api.auth.controller.argumentResolver.AuthCredential;
import com.greedy.mokkoji.common.response.APISuccessResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

@Tag(name = "Admin Controller", description = "관리자 관련 API")
public interface AdminControllerSwagger {
    @Operation(
            summary = "관리자 로그인 API",
            description = "총동연·그리디 개발자 전용 로그인입니다. 카카오 로그인과는 별도 엔드포인트로 운영됩니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "관리자 로그인 성공"),
            @ApiResponse(responseCode = "401", description = "아이디 또는 비밀번호 불일치"),
            @ApiResponse(responseCode = "404", description = "등록되지 않은 계정")
    })
    ResponseEntity<APISuccessResponse<AdminLoginResponse>> login(
            @Parameter(name = "request", description = "관리자 로그인 요청 본문") AdminLoginRequest request
    );

    @Operation(
            summary = "관리자 정보 조회 API",
            description = "로그인한 관리자의 권한과 소속 학교 코드를 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "관리자 정보 조회 성공"),
            @ApiResponse(responseCode = "403", description = "관리자 권한이 없는 사용자"),
            @ApiResponse(responseCode = "404", description = "등록되지 않은 계정")
    })
    ResponseEntity<APISuccessResponse<AdminInfoResponse>> getAdminInfo(
            @Parameter(hidden = true) AuthCredential authCredential
    );
}
