package com.greedy.mokkoji.api.report.controller;

import com.greedy.mokkoji.api.auth.controller.argumentResolver.AuthCredential;
import com.greedy.mokkoji.api.auth.controller.argumentResolver.Authentication;
import com.greedy.mokkoji.api.report.dto.request.ReportRequest;
import com.greedy.mokkoji.api.report.service.ReportService;
import com.greedy.mokkoji.common.response.APISuccessResponse;
import com.greedy.mokkoji.enums.report.ReportType;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("${api.prefix}/reports")
public class ReportController {

    private final ReportService reportService;

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
