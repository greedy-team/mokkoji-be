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

    @Operation(
            summary = "모집글 생성 API",
            description = "해당 동아리에 새로운 모집글을 작성합니다.",
            security = {@SecurityRequirement(name = "JWT")}
    )
    @ApiResponse(responseCode = "201", description = "모집글 생성 성공")
    @Parameter(
            name = "clubId",
            description = "모집글을 작성할 동아리 ID",
            in = ParameterIn.PATH,
            required = true
    )
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

    @Operation(
            summary = "모집글 수정 API",
            description = "기존 모집글의 제목, 내용, 기간 등을 수정합니다.",
            security = {@SecurityRequirement(name = "JWT")}
    )
    @ApiResponse(responseCode = "200", description = "모집글 수정 성공")
    @Parameter(
            name = "recruitmentId",
            description = "수정할 모집글 ID",
            in = ParameterIn.PATH,
            required = true
    )
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

    @Operation(
            summary = "모집글 삭제 API",
            description = "특정 모집글을 삭제합니다.",
            security = {@SecurityRequirement(name = "JWT")}
    )
    @ApiResponse(responseCode = "200", description = "모집글 삭제 성공")
    @Parameter(
            name = "recruitmentId",
            description = "삭제할 모집글 ID",
            in = ParameterIn.PATH,
            required = true
    )
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
            summary = "동아리별 모집글 목록 조회 API",
            description = "특정 동아리의 모든 모집글을 조회합니다."
    )
    @ApiResponse(responseCode = "200", description = "동아리별 모집글 목록 조회 성공")
    @Parameter(
            name = "clubId",
            description = "조회할 동아리 ID",
            in = ParameterIn.PATH,
            required = true
    )
    @GetMapping("/club/{clubId}")
    public ResponseEntity<APISuccessResponse<AllRecruitmentOfClubResponse>> getAllRecruitmentOfClub(
            @PathVariable("clubId") final Long clubId
    ) {
        return APISuccessResponse.of(
                HttpStatus.OK,
                recruitmentService.getAllRecruitmentOfClub(clubId)
        );
    }

    @Operation(
            summary = "특정 모집글 상세 조회 API",
            description = "모집글의 상세 정보를 조회합니다.",
            security = {@SecurityRequirement(name = "JWT")}
    )
    @ApiResponse(responseCode = "200", description = "모집글 상세 조회 성공")
    @Parameter(
            name = "recruitmentId",
            description = "조회할 모집글 ID",
            in = ParameterIn.PATH,
            required = true
    )
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
                    전체 모집글을 조회합니다.  
                    affiliation(소속)과 category(카테고리)를 필터링할 수 있습니다.  
                    페이지네이션(page, size) 파라미터를 반드시 포함해야 합니다.
                    """,
            security = {@SecurityRequirement(name = "JWT")}
    )
    @ApiResponse(responseCode = "200", description = "전체 모집글 조회 성공")
    @Parameter(
            name = "affiliation",
            description = "동아리 소속 (예: CENTRAL, DEPARTMENT 등)",
            in = ParameterIn.QUERY,
            schema = @Schema(implementation = ClubAffiliation.class),
            required = false
    )
    @Parameter(
            name = "category",
            description = "동아리 카테고리 (예: SPORTS, MUSIC 등)",
            in = ParameterIn.QUERY,
            schema = @Schema(implementation = ClubCategory.class),
            required = false
    )
    @Parameter(
            name = "page",
            description = "조회할 페이지 번호 (1부터 시작)",
            in = ParameterIn.QUERY,
            required = true
    )
    @Parameter(
            name = "size",
            description = "페이지당 항목 수",
            in = ParameterIn.QUERY,
            required = true
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
