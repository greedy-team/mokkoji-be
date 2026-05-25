package com.greedy.mokkoji.api.club.controller;

import com.greedy.mokkoji.api.auth.controller.argumentResolver.AuthCredential;
import com.greedy.mokkoji.api.club.dto.response.AdminClubsResponse;
import com.greedy.mokkoji.common.response.APISuccessResponse;
import com.greedy.mokkoji.enums.university.UniversityCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

@Tag(name = "Admin Club Controller", description = "관리자용 동아리 관련 API")
public interface AdminClubControllerSwagger {

    @Operation(
            summary = "관리자용 동아리 목록 조회 API",
            description = "페이지 번호(`page`)는 1부터 시작",
            security = @SecurityRequirement(name = "JWT")
    )
    @ApiResponse(responseCode = "200", description = "조회 성공")
    ResponseEntity<APISuccessResponse<AdminClubsResponse>> getAdminClubs(
            @Parameter(hidden = true) AuthCredential authCredential,
            @Parameter(name = "universityCode", description = "대학교 코드") UniversityCode universityCode,
            @Parameter(name = "page", description = "페이지 번호") int page,
            @Parameter(name = "size", description = "페이지 크기") int size
    );
}
