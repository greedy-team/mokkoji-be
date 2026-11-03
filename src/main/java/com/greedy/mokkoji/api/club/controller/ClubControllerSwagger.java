package com.greedy.mokkoji.api.club.controller;

import com.greedy.mokkoji.api.auth.controller.argumentResolver.AuthCredential;
import com.greedy.mokkoji.api.club.dto.request.ClubSearchCond;
import com.greedy.mokkoji.api.club.dto.request.ClubUpdateRequest;
import com.greedy.mokkoji.api.club.dto.response.ClubDetailResponse;
import com.greedy.mokkoji.api.club.dto.response.ClubUpdateResponse;
import com.greedy.mokkoji.api.club.dto.response.ClubsPaginationResponse;
import com.greedy.mokkoji.api.club.dto.response.allClubs.AllClubsResponse;
import com.greedy.mokkoji.common.response.APISuccessResponse;
import com.greedy.mokkoji.enums.club.ClubAffiliation;
import com.greedy.mokkoji.enums.club.ClubCategory;
import com.greedy.mokkoji.enums.university.UniversityCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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
            summary = "동아리 조회수 수집 API",
            description = "상세 화면 표시 후 호출합니다. 요청 본문은 없습니다. "
                    + "Authorization이 있으면 인증된 계정, 없으면 mokkoji_visitor_id UUID 쿠키로 식별합니다. "
                    + "같은 방문자·동아리는 최초 집계 후 30분간 중복 제외합니다. "
                    + "쿠키가 없거나 유효하지 않으면 새로 발급하므로 프록시는 Set-Cookie를 브라우저에 전달해야 합니다. "
                    + "중복·집계 비활성·Redis 수집 실패도 204로 응답하며 DB 반영 완료를 보장하지 않습니다.",
            security = {@SecurityRequirement(name = "JWT")}
    )
    @ApiResponse(responseCode = "204", description = "수집 요청 처리 종료, 응답 본문 없음")
    @ApiResponse(responseCode = "400", description = "동아리 ID 형식 오류")
    @ApiResponse(responseCode = "401", description = "전달한 인증 토큰이 만료되었거나 유효하지 않음")
    @ApiResponse(responseCode = "404", description = "동아리를 찾을 수 없음")
    ResponseEntity<Void> recordClubView(
            @Parameter(hidden = true) AuthCredential authCredential,
            @Parameter(name = "clubId", description = "동아리 ID") Long clubId,
            @Parameter(hidden = true) HttpServletRequest request,
            @Parameter(hidden = true) HttpServletResponse response
    );

    @Operation(
            summary = "동아리 검색 API",
            description = "페이지 번호(`page`)는 1부터 시작",
            security = {@SecurityRequirement(name = "JWT")}
    )
    @ApiResponse(responseCode = "200", description = "조회 성공")
    ResponseEntity<APISuccessResponse<ClubsPaginationResponse>> getClubs(
            @Parameter(hidden = true) AuthCredential authCredential,
            @Parameter(name = "clubSearchCond", description = "검색 조건") ClubSearchCond clubSearchCond,
            @Parameter(name = "universityCode", description = "대학교 코드") UniversityCode universityCode,
            @Parameter(name = "page", description = "페이지 번호") int page,
            @Parameter(name = "size", description = "페이지 크기") int size
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

    @Operation(
            summary = "전체 동아리 조회 API",
            description = "동아리 리스트를 반환합니다. 페이지 번호(`page`)는 1부터 시작.",
            security = {@SecurityRequirement(name = "JWT")}
    )
    @ApiResponse(responseCode = "200", description = "조회 성공")
    ResponseEntity<APISuccessResponse<AllClubsResponse>> getAllClubs(
            @Parameter(hidden = true) AuthCredential authCredential,
            @Parameter(name = "universityCode", description = "대학교 코드") UniversityCode universityCode,
            @Parameter(name = "keyword", description = "검색 키워드") String keyword,
            @Parameter(name = "affiliation") ClubAffiliation affiliation,
            @Parameter(name = "category") ClubCategory category,
            @Parameter(name = "page", description = "페이지 번호") int page,
            @Parameter(name = "size", description = "페이지 크기") int size
    );

}
