package com.greedy.mokkoji.api.report.controller;

import com.greedy.mokkoji.api.auth.controller.argumentResolver.AuthCredential;
import com.greedy.mokkoji.api.report.dto.request.ReportRequest;
import com.greedy.mokkoji.common.response.APISuccessResponse;
import com.greedy.mokkoji.enums.report.ReportType;
import io.swagger.v3.oas.annotations.*;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
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
    @Parameter(
            name = "reportType",
            in = ParameterIn.QUERY,
            required = true,
            schema = @Schema(implementation = ReportType.class)
    )
    ResponseEntity<APISuccessResponse<Void>> createReport(
            @Parameter(hidden = true) AuthCredential authCredential,
            @Parameter(name = "reportType", description = "신고 대상 유형") ReportType reportType,
            @Parameter(name = "reportRequest", description = "신고 내용") ReportRequest reportRequest
    );
}
