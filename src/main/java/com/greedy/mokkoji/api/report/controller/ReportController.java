package com.greedy.mokkoji.api.report.controller;

import com.greedy.mokkoji.api.auth.controller.argumentResolver.AuthCredential;
import com.greedy.mokkoji.api.auth.controller.argumentResolver.Authentication;
import com.greedy.mokkoji.api.report.dto.request.ReportRequest;
import com.greedy.mokkoji.api.report.service.ReportService;
import com.greedy.mokkoji.common.response.APISuccessResponse;
import com.greedy.mokkoji.enums.report.ReportType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("${api.prefix}/reports")
public class ReportController {

    private final ReportService reportService;

    @Operation(
            summary = "신고 생성 API",
            description = """
                    사용자가 특정 대상을 신고합니다.  
                    reportType은 다음 중 하나를 입력해야 합니다:  
                    - CLUB (동아리)  
                    - RECRUITMENT (모집글)  
                    - COMMENT (댓글)  
                    - RATING (평점)
                    """,
            security = {@SecurityRequirement(name = "JWT")}
    )
    @ApiResponse(responseCode = "201", description = "신고 생성 성공")
    @Parameter(
            name = "reportType",
            description = "신고 대상 유형",
            in = ParameterIn.QUERY,
            required = true,
            schema = @Schema(implementation = ReportType.class, example = "COMMENT")
    )
    @PostMapping
    public ResponseEntity<APISuccessResponse<Void>> createReport(
            @Authentication final AuthCredential authCredential,
            @RequestParam(name = "reportType") final ReportType reportType,
            @RequestBody final ReportRequest reportRequest
    ) {
        return APISuccessResponse.of(
                HttpStatus.CREATED,
                reportService.createReport(authCredential.userId(), reportType, reportRequest.content())
        );
    }
}
