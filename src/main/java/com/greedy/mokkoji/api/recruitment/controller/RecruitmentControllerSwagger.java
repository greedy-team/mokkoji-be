package com.greedy.mokkoji.api.recruitment.controller;

import com.greedy.mokkoji.api.auth.controller.argumentResolver.AuthCredential;
import com.greedy.mokkoji.api.recruitment.dto.request.CreateRecruitmentRequest;
import com.greedy.mokkoji.api.recruitment.dto.request.UpdateRecruitmentRequest;
import com.greedy.mokkoji.api.recruitment.dto.response.AllRecruitment.AllRecruitmentResponse;
import com.greedy.mokkoji.api.recruitment.dto.response.allRecruitmentOfClub.AllRecruitmentOfClubResponse;
import com.greedy.mokkoji.api.recruitment.dto.response.createRecruitment.CreateRecruitmentResponse;
import com.greedy.mokkoji.api.recruitment.dto.response.deleteRecruitment.DeleteRecruitmentResponse;
import com.greedy.mokkoji.api.recruitment.dto.response.specificRecruitment.SpecificRecruitmentResponse;
import com.greedy.mokkoji.api.recruitment.dto.response.updateRecruitment.UpdateRecruitmentResponse;
import com.greedy.mokkoji.common.response.APISuccessResponse;
import com.greedy.mokkoji.enums.club.ClubAffiliation;
import com.greedy.mokkoji.enums.club.ClubCategory;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

@Tag(name = "Recruitment Controller", description = "모집글 관련 API")
public interface RecruitmentControllerSwagger {

    @Operation(
            summary = "모집글 생성 API",
            security = {@SecurityRequirement(name = "JWT")}
    )
    @ApiResponse(responseCode = "201", description = "모집글 생성 성공")
    ResponseEntity<APISuccessResponse<CreateRecruitmentResponse>> createRecruitment(
            @Parameter(hidden = true) AuthCredential authCredential,
            @Parameter(name = "clubId", description = "모집글을 작성할 동아리 ID") Long clubId,
            @Parameter(name = "request", description = "모집글 생성 요청 본문") CreateRecruitmentRequest request
    );

    @Operation(
            summary = "모집글 수정 API",
            security = {@SecurityRequirement(name = "JWT")}
    )
    @ApiResponse(responseCode = "200", description = "모집글 수정 성공")
    ResponseEntity<APISuccessResponse<UpdateRecruitmentResponse>> updateRecruitment(
            @Parameter(hidden = true) AuthCredential authCredential,
            @Parameter(name = "recruitmentId", description = "수정할 모집글 ID") Long recruitmentId,
            @Parameter(name = "request", description = "모집글 수정 요청 본문") UpdateRecruitmentRequest request
    );

    @Operation(
            summary = "모집글 삭제 API",
            security = {@SecurityRequirement(name = "JWT")}
    )
    @ApiResponse(responseCode = "200", description = "모집글 삭제 성공")
    ResponseEntity<APISuccessResponse<DeleteRecruitmentResponse>> deleteRecruitment(
            @Parameter(hidden = true) AuthCredential authCredential,
            @Parameter(name = "recruitmentId", description = "삭제할 모집글 ID") Long recruitmentId
    );

    @Operation(
            summary = "특정 동아리의 모든 모집글 조회 API"
    )
    @ApiResponse(responseCode = "200", description = "동아리의 모집글 목록 조회 성공")
    ResponseEntity<APISuccessResponse<AllRecruitmentOfClubResponse>> getAllRecruitmentOfClub(
            @Parameter(name = "clubId", description = "조회할 동아리 ID") Long clubId
    );

    @Operation(
            summary = "특정 모집글 상세 조회 API",
            security = {@SecurityRequirement(name = "JWT")}
    )
    @ApiResponse(responseCode = "200", description = "모집글 상세 조회 성공")
    ResponseEntity<APISuccessResponse<SpecificRecruitmentResponse>> getSpecificRecruitment(
            @Parameter(hidden = true) AuthCredential authCredential,
            @Parameter(name = "recruitmentId", description = "조회할 모집글 ID") Long recruitmentId
    );

    @Operation(
            summary = "전체 모집글 조회 API",
            security = @SecurityRequirement(name = "JWT")
    )
    @ApiResponse(responseCode = "200", description = "전체 모집글 조회 성공")
    @Parameters({
            @Parameter(
                    name = "affiliation",
                    in = ParameterIn.QUERY,
                    schema = @Schema(implementation = ClubAffiliation.class),
                    required = false
            ),
            @Parameter(
                    name = "category",
                    in = ParameterIn.QUERY,
                    schema = @Schema(implementation = ClubCategory.class),
                    required = false
            ),
            @Parameter(name = "page", description = "조회할 페이지 번호 (1부터 시작)", in = ParameterIn.QUERY, required = true),
            @Parameter(name = "size", description = "한 페이지당 항목 수", in = ParameterIn.QUERY, required = true)
    })
    ResponseEntity<APISuccessResponse<AllRecruitmentResponse>> getAllRecruitment(
            @Parameter(hidden = true) AuthCredential authCredential,
            ClubAffiliation affiliation,
            ClubCategory category,
            int page,
            int size
    );
}
