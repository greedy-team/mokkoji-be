package com.greedy.mokkoji.api.university.controller;

import com.greedy.mokkoji.api.university.dto.response.UniversitiesResponse;
import com.greedy.mokkoji.common.response.APISuccessResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

@Tag(name = "University Controller", description = "학교 관련 API")
public interface UniversityControllerSwagger {

    @Operation(summary = "서비스 중인 학교 목록 조회 API")
    @ApiResponse(responseCode = "200", description = "학교 목록 조회 성공")
    ResponseEntity<APISuccessResponse<UniversitiesResponse>> getUniversities();
}
