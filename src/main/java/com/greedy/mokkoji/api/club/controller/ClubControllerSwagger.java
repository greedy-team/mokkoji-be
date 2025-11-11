package com.greedy.mokkoji.api.club.controller;

import com.greedy.mokkoji.api.auth.controller.argumentResolver.AuthCredential;
import com.greedy.mokkoji.api.club.dto.request.ClubCreateRequest;
import com.greedy.mokkoji.api.club.dto.request.ClubSearchCond;
import com.greedy.mokkoji.api.club.dto.request.ClubUpdateRequest;
import com.greedy.mokkoji.api.club.dto.response.ClubDetailResponse;
import com.greedy.mokkoji.api.club.dto.response.ClubManageDetailResponse;
import com.greedy.mokkoji.api.club.dto.response.ClubUpdateResponse;
import com.greedy.mokkoji.api.club.dto.response.ClubsPaginationResponse;
import com.greedy.mokkoji.common.response.APISuccessResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

@Tag(name = "Club Controller", description = "동아리 관련 API")
public interface ClubControllerSwagger {

    @Operation(
            summary = "동아리 상세 조회 API",
            security = {@SecurityRequirement(name = "JWT")}
    )
    @ApiResponse(responseCode = "200", description = "조회 성공")
    ResponseEntity<APISuccessResponse<ClubDetailResponse>> getClub(
            @Parameter(hidden = true) AuthCredential authCredential,
            @Parameter(name = "clubId", description = "동아리 ID") Long clubId
    );

    @Operation(
            summary = "동아리 목록 조회 API",
            description = "페이지 번호(`page`)는 1부터 시작",
            security = {@SecurityRequirement(name = "JWT")}
    )
    @ApiResponse(responseCode = "200", description = "조회 성공")
    ResponseEntity<APISuccessResponse<ClubsPaginationResponse>> getClubs(
            @Parameter(hidden = true) AuthCredential authCredential,
            @Parameter(name = "clubSearchCond", description = "검색 조건") ClubSearchCond clubSearchCond,
            @Parameter(name = "page", description = "페이지 번호") int page,
            @Parameter(name = "size", description = "페이지 크기") int size
    );

    @Operation(
            summary = "동아리 생성 API",
            security = {@SecurityRequirement(name = "JWT")}
    )
    @ApiResponse(responseCode = "201", description = "동아리 생성 성공")
    ResponseEntity<APISuccessResponse<Void>> createClub(
            @Parameter(name = "clubCreateRequest", description = "동아리 생성 요청") ClubCreateRequest clubCreateRequest,
            @Parameter(hidden = true) AuthCredential authCredential
    );

    @Operation(
            summary = "사용자가 관리 중인 동아리 상세 조회 API",
            security = {@SecurityRequirement(name = "JWT")}
    )
    @ApiResponse(responseCode = "200", description = "조회 성공")
    ResponseEntity<APISuccessResponse<ClubManageDetailResponse>> getClubManageDetail(
            @Parameter(name = "clubId", description = "동아리 ID") Long clubId,
            @Parameter(hidden = true) AuthCredential authCredential
    );

    @Operation(
            summary = "동아리 수정 API",
            security = {@SecurityRequirement(name = "JWT")}
    )
    @ApiResponse(responseCode = "200", description = "수정 성공")
    ResponseEntity<APISuccessResponse<ClubUpdateResponse>> updateClub(
            @Parameter(name = "clubId", description = "동아리 ID") Long clubId,
            @Parameter(hidden = true) AuthCredential authCredential,
            @Parameter(name = "clubUpdateRequest", description = "동아리 수정 요청") ClubUpdateRequest clubUpdateRequest
    );
}
