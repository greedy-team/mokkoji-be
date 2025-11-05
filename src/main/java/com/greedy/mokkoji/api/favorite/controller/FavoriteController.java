package com.greedy.mokkoji.api.favorite.controller;

import com.greedy.mokkoji.api.auth.controller.argumentResolver.AuthCredential;
import com.greedy.mokkoji.api.auth.controller.argumentResolver.Authentication;
import com.greedy.mokkoji.api.club.dto.response.ClubsPaginationResponse;
import com.greedy.mokkoji.api.favorite.dto.response.RecruitClubsResponse;
import com.greedy.mokkoji.api.favorite.service.FavoriteService;
import com.greedy.mokkoji.common.response.APISuccessResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.YearMonth;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("${api.prefix}/favorites")
public class FavoriteController {

    private final FavoriteService favoriteService;

    @Operation(summary = "즐겨찾기 추가 API", security = {@SecurityRequirement(name = "JWT")})
    @ApiResponse(responseCode = "201", description = "즐겨찾기 추가 성공")
    @PostMapping("/{clubId}")
    public ResponseEntity<APISuccessResponse<Void>> addFavorite(
            @Authentication final AuthCredential authCredential,
            @PathVariable(name = "clubId") final Long clubId
    ) {
        return APISuccessResponse.of(HttpStatus.CREATED, favoriteService.addFavorite(authCredential.userId(), clubId));
    }

    @Operation(summary = "즐겨찾기 목록 조회 API", description = " 페이지네이션(`page`, `size`) 파라미터를 반드시 포함해야 합니다.", security = {@SecurityRequirement(name = "JWT")})
    @ApiResponse(responseCode = "200", description = "즐겨찾기 목록 조회 성공")
    @GetMapping
    public ResponseEntity<APISuccessResponse<ClubsPaginationResponse>> getFavoriteClubs(
            @Authentication final AuthCredential authCredential,
            @RequestParam(value = "page") final int page,
            @RequestParam(value = "size") final int size
    ) {
        final Pageable pageable = PageRequest.of(page - 1, size);
        return APISuccessResponse.of(HttpStatus.OK, favoriteService.findFavoriteClubs(authCredential.userId(), pageable));
    }

    @Operation(summary = "즐겨찾기 삭제 API", security = {@SecurityRequirement(name = "JWT")})
    @ApiResponse(responseCode = "204", description = "즐겨찾기 삭제 성공 (No Content)")
    @DeleteMapping("/{clubId}")
    public ResponseEntity<APISuccessResponse<Void>> deleteFavorite(
            @Authentication final AuthCredential authCredential,
            @PathVariable(name = "clubId") final Long clubId
    ) {
        return APISuccessResponse.of(HttpStatus.NO_CONTENT, favoriteService.deleteFavorite(authCredential.userId(), clubId));
    }

    @Operation(
            summary = "모집 중인 즐겨찾기 동아리 조회 API",
            description = "사용자가 즐겨찾기한 동아리 중, 특정 연월(`yearMonth`)에 모집 중인 동아리를 조회합니다. ex: `yearMonth=2025-11`",
            security = {@SecurityRequirement(name = "JWT")}
    )
    @ApiResponse(responseCode = "200", description = "모집 중인 즐겨찾기 동아리 조회 성공")
    @GetMapping("/recruit")
    public ResponseEntity<APISuccessResponse<List<RecruitClubsResponse>>> getRecruitClubs(
            @Authentication final AuthCredential authCredential,
            @RequestParam(name = "yearMonth") YearMonth yearMonth
    ) {
        return APISuccessResponse.of(HttpStatus.OK, favoriteService.getRecruitClubs(authCredential.userId(), yearMonth));
    }
}
