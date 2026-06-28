package com.greedy.mokkoji.api.user.controller;

import com.greedy.mokkoji.api.auth.controller.argumentResolver.AuthCredential;
import com.greedy.mokkoji.api.user.dto.request.UpdateUserInformationRequest;
import com.greedy.mokkoji.api.user.dto.request.kakao.KakaoSocialLoginRequest;
import com.greedy.mokkoji.api.user.dto.resopnse.*;
import com.greedy.mokkoji.common.response.APISuccessResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

@Tag(name = "User Controller", description = "사용자 관련 API")
public interface UserControllerSwagger {

    @Operation(
            summary = "카카오 로그인 API",
            description = "프론트에서 카카오 OAuth 완료 후 받은 `authorization code`를 전달하여 로그인을 수행합니다. "
    )
    @ApiResponse(responseCode = "200", description = "카카오 로그인 성공")
    ResponseEntity<APISuccessResponse<LoginResponse>> kakaoLogin(
            @Parameter(name = "request", description = "카카오 로그인 요청 본문") KakaoSocialLoginRequest request
    );

    @Operation(
            summary = "Access Token 재발급 API",
            description = "`Refresh Token`을 사용해 새로운 `Access Token`을 발급받습니다."
    )
    @ApiResponse(responseCode = "200", description = "Access Token 재발급 성공")
    @Parameter(
            name = "Authorization",
            description = "Refresh Token (예: Bearer eyJhbGci...)",
            in = ParameterIn.HEADER,
            required = true
    )
    ResponseEntity<APISuccessResponse<RefreshResponse>> refresh(
            @Parameter(name = "Authorization", description = "Refresh Token 헤더") String bearerToken
    );

    @Operation(
            summary = "로그아웃 API",
            security = {@SecurityRequirement(name = "JWT")}
    )
    @ApiResponse(responseCode = "200", description = "로그아웃 성공")
    ResponseEntity<APISuccessResponse<Void>> logout(
            @Parameter(hidden = true) AuthCredential authCredential
    );

    @Operation(
            summary = "사용자 정보 조회 API",
            security = {@SecurityRequirement(name = "JWT")}
    )
    @ApiResponse(responseCode = "200", description = "사용자 정보 조회 성공")
    ResponseEntity<APISuccessResponse<UserInformationResponse>> getUserInformation(
            @Parameter(hidden = true) AuthCredential authCredential
    );

    @Operation(
            summary = "사용자 정보 수정 API",
            description = "전달된 필드만 수정됩니다. 대학교가 변경되는 경우 기존 즐겨찾기는 모두 삭제됩니다.\n"
                    + "학교 정보를 초기화하려면 `clearUniversityCode: true`를 전달하세요. `universityCode`와 함께 전달된 경우 `clearUniversityCode`가 우선 적용됩니다.",
            security = {@SecurityRequirement(name = "JWT")}
    )
    @ApiResponse(responseCode = "200", description = "사용자 정보 수정 성공")
    ResponseEntity<APISuccessResponse<Void>> updateUserInformation(
            @Parameter(hidden = true) AuthCredential authCredential,
            @Parameter(name = "updateUserInformationRequest", description = "사용자 정보 수정 요청") UpdateUserInformationRequest updateUserInformationRequest
    );

    @Operation(
            summary = "회원 탈퇴 API",
            description = "회원과 회원의 연관 데이터를 삭제합니다.\n"
                    + "* 댓글은 삭제하지 않습니다.\n"
                    + "* 탈퇴 회원이 동아리장인 경우 해당 동아리의 동아리장은 해제됩니다.",
            security = {@SecurityRequirement(name = "JWT")}
    )
    @ApiResponse(responseCode = "200", description = "회원 탈퇴 성공")
    ResponseEntity<APISuccessResponse<Void>> deleteUser(
            @Parameter(hidden = true) AuthCredential authCredential
    );

    @Operation(
            summary = "사용자 권한 조회 API",
            security = {@SecurityRequirement(name = "JWT")}
    )
    @ApiResponse(responseCode = "200", description = "권한 조회 성공")
    ResponseEntity<APISuccessResponse<UserRoleResponse>> getUserRole(
            @Parameter(hidden = true) AuthCredential authCredential
    );

    @Operation(summary = "사용자의 관리 중인 동아리 조회 API", security = {@SecurityRequirement(name = "JWT")})
    @ApiResponse(responseCode = "200", description = "관리 동아리 조회 성공")
    ResponseEntity<APISuccessResponse<UserManageClubsResponse>> getUserManageClubs(
            @Parameter(hidden = true) AuthCredential authCredential
    );
}
