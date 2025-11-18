package com.greedy.mokkoji.api.favorite.controller;

import com.greedy.mokkoji.api.auth.controller.argumentResolver.AuthCredential;
import com.greedy.mokkoji.api.club.dto.response.ClubsPaginationResponse;
import com.greedy.mokkoji.api.favorite.dto.response.RecruitClubsResponse;
import com.greedy.mokkoji.common.response.APISuccessResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

import java.time.YearMonth;
import java.util.List;

@Tag(name = "Favorite Controller", description = "즐겨찾기 관련 API")
public interface FavoriteControllerSwagger {

    @Operation(
            summary = "즐겨찾기 추가 API",
            security = {@SecurityRequirement(name = "JWT")}
    )
    @ApiResponse(responseCode = "201", description = "즐겨찾기 추가 성공")
    ResponseEntity<APISuccessResponse<Void>> addFavorite(
            @Parameter(hidden = true) AuthCredential authCredential,
            @Parameter(name = "clubId", description = "즐겨찾기로 추가할 동아리 ID") Long clubId
    );

    @Operation(
            summary = "즐겨찾기 목록 조회 API",
            description = "페이지 번호(`page`)는 1부터 시작",
            security = {@SecurityRequirement(name = "JWT")}
    )
    @ApiResponse(responseCode = "200", description = "즐겨찾기 목록 조회 성공")
    ResponseEntity<APISuccessResponse<ClubsPaginationResponse>> getFavoriteClubs(
            @Parameter(hidden = true) AuthCredential authCredential,
            @Parameter(name = "page", description = "페이지 번호") int page,
            @Parameter(name = "size", description = "페이지 크기") int size
    );

    @Operation(
            summary = "즐겨찾기 삭제 API",
            security = {@SecurityRequirement(name = "JWT")}
    )
    @ApiResponse(responseCode = "204", description = "즐겨찾기 삭제 성공")
    ResponseEntity<APISuccessResponse<Void>> deleteFavorite(
            @Parameter(hidden = true) AuthCredential authCredential,
            @Parameter(name = "clubId", description = "즐겨찾기에서 삭제할 동아리 ID") Long clubId
    );

    @Operation(
            summary = "모집 중인 즐겨찾기 동아리 조회 API",
            description = "사용자가 즐겨찾기한 동아리 중, 특정 연월(`yearMonth`)에 모집 중인 동아리를 조회합니다.",
            security = {@SecurityRequirement(name = "JWT")}
    )
    @ApiResponse(responseCode = "200", description = "모집 중인 즐겨찾기 동아리 조회 성공")
    ResponseEntity<APISuccessResponse<List<RecruitClubsResponse>>> getRecruitClubs(
            @Parameter(hidden = true) AuthCredential authCredential,
            @Parameter(name = "yearMonth", description = "조회할 연월 (예: 2025-11)") YearMonth yearMonth
    );
}
