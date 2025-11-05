package com.greedy.mokkoji.api.club.controller;

import com.greedy.mokkoji.api.auth.controller.argumentResolver.AuthCredential;
import com.greedy.mokkoji.api.auth.controller.argumentResolver.Authentication;
import com.greedy.mokkoji.api.club.dto.request.ClubCreateRequest;
import com.greedy.mokkoji.api.club.dto.request.ClubSearchCond;
import com.greedy.mokkoji.api.club.dto.request.ClubUpdateRequest;
import com.greedy.mokkoji.api.club.dto.response.ClubDetailResponse;
import com.greedy.mokkoji.api.club.dto.response.ClubManageDetailResponse;
import com.greedy.mokkoji.api.club.dto.response.ClubUpdateResponse;
import com.greedy.mokkoji.api.club.dto.response.ClubsPaginationResponse;
import com.greedy.mokkoji.api.club.service.ClubService;
import com.greedy.mokkoji.common.response.APISuccessResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("${api.prefix}/clubs")
public class ClubController {

    private final ClubService clubService;

    @Operation(
            summary = "동아리 상세 조회 API",
            description = "특정 동아리의 상세 정보를 조회합니다.",
            security = {@SecurityRequirement(name = "JWT")}
    )
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @GetMapping("/{clubId}")
    public ResponseEntity<APISuccessResponse<ClubDetailResponse>> getClub(
            @Parameter(hidden = true)
            @Authentication final AuthCredential authCredential,
            @PathVariable(name = "clubId") final Long clubId
    ) {
        return APISuccessResponse.of(
                HttpStatus.OK,
                clubService.findClub(authCredential.userId(), clubId)
        );
    }

    @Operation(
            summary = "동아리 목록 조회 API",
            description = "검색 조건과 페이징 정보를 이용해 동아리 목록을 조회합니다.",
            security = {@SecurityRequirement(name = "JWT")}
    )
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @GetMapping
    public ResponseEntity<APISuccessResponse<ClubsPaginationResponse>> getClubs(
            @Authentication final AuthCredential authCredential,
            @ModelAttribute(value = "clubSearchCond") final ClubSearchCond clubSearchCond,
            @RequestParam(value = "page") final int page,
            @RequestParam(value = "size") final int size
    ) {
        final Pageable pageable = PageRequest.of(page - 1, size);

        return APISuccessResponse.of(
                HttpStatus.OK,
                clubService.findClubsByConditions(
                        authCredential.userId(),
                        clubSearchCond.keyword(),
                        clubSearchCond.category(),
                        clubSearchCond.affiliation(),
                        clubSearchCond.recruitStatus(),
                        pageable
                )
        );
    }

    @Operation(
            summary = "동아리 생성 API",
            description = "새로운 동아리를 생성합니다.",
            security = {@SecurityRequirement(name = "JWT")}
    )
    @ApiResponse(responseCode = "201", description = "동아리 생성 성공")
    @PostMapping
    public ResponseEntity<APISuccessResponse<Void>> createClub(
            @RequestBody final ClubCreateRequest clubCreateRequest,
            @Authentication final AuthCredential authCredential
    ) {
        clubService.createClub(
                authCredential.userId(),
                clubCreateRequest.name(),
                clubCreateRequest.category(),
                clubCreateRequest.affiliation(),
                clubCreateRequest.clubMasterStudentId()
        );
        return APISuccessResponse.of(HttpStatus.CREATED, null);
    }

    @Operation(
            summary = "관리 중인 동아리 상세 조회 API",
            description = "해당 사용자가 관리 중인 동아리의 상세 정보를 조회합니다.",
            security = {@SecurityRequirement(name = "JWT")}
    )
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @GetMapping("/manage/{clubId}")
    public ResponseEntity<APISuccessResponse<ClubManageDetailResponse>> getClubManageDetail(
            @PathVariable(name = "clubId") final Long clubId,
            @Authentication final AuthCredential authCredential
    ) {
        return APISuccessResponse.of(
                HttpStatus.OK,
                clubService.getClubManageDetail(authCredential.userId(), clubId)
        );
    }

    @Operation(
            summary = "동아리 수정 API",
            description = "관리자가 동아리 정보를 수정합니다.",
            security = {@SecurityRequirement(name = "JWT")}
    )
    @ApiResponse(responseCode = "200", description = "수정 성공")
    @PatchMapping("/manage/{clubId}")
    public ResponseEntity<APISuccessResponse<ClubUpdateResponse>> updateClub(
            @PathVariable(name = "clubId") final Long clubId,
            @Authentication final AuthCredential authCredential,
            @RequestBody final ClubUpdateRequest clubUpdateRequest
    ) {
        return APISuccessResponse.of(
                HttpStatus.OK,
                clubService.updateClub(
                        authCredential.userId(),
                        clubId,
                        clubUpdateRequest.name(),
                        clubUpdateRequest.category(),
                        clubUpdateRequest.affiliation(),
                        clubUpdateRequest.description(),
                        clubUpdateRequest.clubMasterStudentId(),
                        clubUpdateRequest.logo(),
                        clubUpdateRequest.instagram()
                )
        );
    }
}
