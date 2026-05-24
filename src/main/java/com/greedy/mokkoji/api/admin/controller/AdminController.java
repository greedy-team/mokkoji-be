package com.greedy.mokkoji.api.admin.controller;

import com.greedy.mokkoji.api.admin.dto.request.AdminLoginRequest;
import com.greedy.mokkoji.api.admin.dto.response.AdminLoginResponse;
import com.greedy.mokkoji.api.admin.service.AdminAuthService;
import com.greedy.mokkoji.common.response.APISuccessResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("${api.prefix}/admin")
public class AdminController implements AdminControllerSwagger {

    private final AdminAuthService adminAuthService;

    @PostMapping("/auth/login")
    public ResponseEntity<APISuccessResponse<AdminLoginResponse>> login(
            @RequestBody @Valid final AdminLoginRequest request
    ) {
        final AdminLoginResponse response = adminAuthService.login(request.loginId(), request.password());
        return APISuccessResponse.of(HttpStatus.OK, response);
    }
}
