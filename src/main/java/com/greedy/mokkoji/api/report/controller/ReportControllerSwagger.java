package com.greedy.mokkoji.api.report.controller;

import com.greedy.mokkoji.api.auth.controller.argumentResolver.AuthCredential;
import com.greedy.mokkoji.api.report.dto.request.ReportRequest;
import com.greedy.mokkoji.common.response.APISuccessResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

@Tag(name = "Report Controller", description = "신고 관련 API")
public interface ReportControllerSwagger {

    @Operation(
            summary = "신고 생성 API",
            security = {@SecurityRequirement(name = "JWT")}
    )
    @ApiResponse(responseCode = "201", description = "신고 생성 성공")
    ResponseEntity<APISuccessResponse<Void>> createReport(
            @Parameter(hidden = true) AuthCredential authCredential,
            @Parameter(name = "reportRequest", description = "신고 내용") ReportRequest reportRequest
    );
}
