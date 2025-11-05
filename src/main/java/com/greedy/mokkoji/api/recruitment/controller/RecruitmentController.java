package com.greedy.mokkoji.api.recruitment.controller;

import com.greedy.mokkoji.api.auth.controller.argumentResolver.AuthCredential;
import com.greedy.mokkoji.api.auth.controller.argumentResolver.Authentication;
import com.greedy.mokkoji.api.recruitment.dto.request.CreateRecruitmentRequest;
import com.greedy.mokkoji.api.recruitment.dto.request.UpdateRecruitmentRequest;
import com.greedy.mokkoji.api.recruitment.dto.response.AllRecruitment.AllRecruitmentResponse;
import com.greedy.mokkoji.api.recruitment.dto.response.allRecruitmentOfClub.AllRecruitmentOfClubResponse;
import com.greedy.mokkoji.api.recruitment.dto.response.createRecruitment.CreateRecruitmentResponse;
import com.greedy.mokkoji.api.recruitment.dto.response.deleteRecruitment.DeleteRecruitmentResponse;
import com.greedy.mokkoji.api.recruitment.dto.response.specificRecruitment.SpecificRecruitmentResponse;
import com.greedy.mokkoji.api.recruitment.dto.response.updateRecruitment.UpdateRecruitmentResponse;
import com.greedy.mokkoji.api.recruitment.service.RecruitmentService;
import com.greedy.mokkoji.common.response.APISuccessResponse;
import com.greedy.mokkoji.enums.club.ClubAffiliation;
import com.greedy.mokkoji.enums.club.ClubCategory;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
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
@RequestMapping("${api.prefix}/recruitments")
public class RecruitmentController {

    private final RecruitmentService recruitmentService;

    @Operation(summary = "모집글 생성 API", security = {@SecurityRequirement(name = "JWT")})
    @ApiResponse(responseCode = "201", description = "모집글 생성 성공")
    @PostMapping("/{clubId}")
    public ResponseEntity<APISuccessResponse<CreateRecruitmentResponse>> createRecruitment(
            @Authentication final AuthCredential authCredential,
            @PathVariable("clubId") final Long clubId,
            @RequestBody CreateRecruitmentRequest request
    ) {
        return APISuccessResponse.of(
                HttpStatus.CREATED,
                recruitmentService.createRecruitment(
                        authCredential.userId(),
                        clubId,
                        request.title(),
                        request.content(),
                        request.recruitStart(),
                        request.recruitEnd(),
                        request.images(),
                        request.recruitForm()
                )
        );
    }

    @Operation(summary = "모집글 수정 API", security = {@SecurityRequirement(name = "JWT")})
    @ApiResponse(responseCode = "200", description = "모집글 수정 성공")
    @PatchMapping("/{recruitmentId}")
    public ResponseEntity<APISuccessResponse<UpdateRecruitmentResponse>> updateRecruitment(
            @Authentication final AuthCredential authCredential,
            @PathVariable("recruitmentId") final Long recruitmentId,
            @RequestBody UpdateRecruitmentRequest request
    ) {
        UpdateRecruitmentResponse response = recruitmentService.updateRecruitment(
                authCredential.userId(),
                recruitmentId,
                request.title(),
                request.content(),
                request.recruitStart(),
                request.recruitEnd(),
                request.images(),
                request.recruitForm()
        );
        return APISuccessResponse.of(HttpStatus.OK, response);
    }

    @Operation(summary = "모집글 삭제 API", security = {@SecurityRequirement(name = "JWT")})
    @ApiResponse(responseCode = "200", description = "모집글 삭제 성공")
    @DeleteMapping("/{recruitmentId}")
    public ResponseEntity<APISuccessResponse<DeleteRecruitmentResponse>> deleteRecruitment(
            @Authentication final AuthCredential authCredential,
            @PathVariable("recruitmentId") final Long recruitmentId
    ) {
        DeleteRecruitmentResponse response =
                recruitmentService.deleteRecruitment(authCredential.userId(), recruitmentId);
        return APISuccessResponse.of(HttpStatus.OK, response);
    }

    @Operation(
            summary = "특정 동아리의 모든 모집글 조회 API",
            description = """
                    모집 상태(`RecruitStatus`)
                    - IMMINENT : 모집 마감 임박  
                    - OPEN : 모집 중  
                    - BEFORE : 모집 시작 전  
                    - CLOSED : 모집 종료
                    """
    )
    @ApiResponse(responseCode = "200", description = "동아리의 모집글 목록 조회 성공")
    @GetMapping("/club/{clubId}")
    public ResponseEntity<APISuccessResponse<AllRecruitmentOfClubResponse>> getAllRecruitmentOfClub(
            @PathVariable("clubId") final Long clubId
    ) {
        return APISuccessResponse.of(
                HttpStatus.OK,
                recruitmentService.getAllRecruitmentOfClub(clubId)
        );
    }

    @Operation(summary = "특정 모집글 상세 조회 API", security = {@SecurityRequirement(name = "JWT")})
    @ApiResponse(responseCode = "200", description = "모집글 상세 조회 성공")
    @GetMapping("/{recruitmentId}")
    public ResponseEntity<APISuccessResponse<SpecificRecruitmentResponse>> getSpecificRecruitment(
            @Authentication final AuthCredential authCredential,
            @PathVariable("recruitmentId") final Long recruitmentId
    ) {
        return APISuccessResponse.of(
                HttpStatus.OK,
                recruitmentService.getSpecificRecruitment(authCredential.userId(), recruitmentId)
        );
    }

    @Operation(
            summary = "전체 모집글 조회 API",
            description = """
                    모집 상태(`RecruitStatus`)
                    - IMMINENT : 모집 마감 임박  
                    - OPEN : 모집 중  
                    - BEFORE : 모집 시작 전  
                    - CLOSED : 모집 종료
                    """,
            security = @SecurityRequirement(name = "JWT")
    )
    @ApiResponse(responseCode = "200", description = "전체 모집글 조회 성공")
    @Parameter(
            name = "affiliation",
            description = """
                    동아리 소속  
                    - CENTRAL_CLUB (중앙)  
                    - DEPARTMENT_CLUB (정인준/가인준)  
                    - SMALL_GROUP (소모임)
                    """,
            in = ParameterIn.QUERY,
            schema = @Schema(implementation = ClubAffiliation.class),
            required = false
    )
    @Parameter(
            name = "category",
            description = """
                    동아리 카테고리  
                    - CULTURAL_ART (문화/예술)  
                    - ACADEMIC_CULTURAL (학술/교양)  
                    - VOLUNTEER_SOCIAL (봉사/사회)  
                    - SPORTS (체육)  
                    - RELIGIOUS (종교)  
                    - OTHER (기타)
                    """,
            in = ParameterIn.QUERY,
            schema = @Schema(implementation = ClubCategory.class),
            required = false
    )
    @GetMapping
    public ResponseEntity<APISuccessResponse<AllRecruitmentResponse>> getAllRecruitment(
            @Authentication final AuthCredential authCredential,
            @RequestParam(value = "affiliation", required = false) final ClubAffiliation affiliation,
            @RequestParam(value = "category", required = false) final ClubCategory category,
            @RequestParam(value = "page") final int page,
            @RequestParam(value = "size") final int size
    ) {
        final Pageable pageable = PageRequest.of(page - 1, size);
        return APISuccessResponse.of(
                HttpStatus.OK,
                recruitmentService.getAllRecruitment(authCredential.userId(), affiliation, category, pageable)
        );
    }

}
