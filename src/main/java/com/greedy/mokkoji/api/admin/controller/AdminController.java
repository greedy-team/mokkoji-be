package com.greedy.mokkoji.api.admin.controller;

import com.greedy.mokkoji.api.admin.dto.request.AdminLoginRequest;
import com.greedy.mokkoji.api.admin.dto.response.AdminLoginResponse;
import com.greedy.mokkoji.api.admin.dto.response.AdminInfoResponse;
import com.greedy.mokkoji.api.admin.service.AdminService;
import com.greedy.mokkoji.api.auth.controller.argumentResolver.AuthCredential;
import com.greedy.mokkoji.api.auth.controller.argumentResolver.Authentication;
import com.greedy.mokkoji.common.response.APISuccessResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("${api.prefix}/admin")
public class AdminController implements AdminControllerSwagger {
    private final AdminService adminAuthService;

    @PostMapping("/auth/login")
    public ResponseEntity<APISuccessResponse<AdminLoginResponse>> login(
            @RequestBody @Valid final AdminLoginRequest request
    ) {
        final AdminLoginResponse response = adminAuthService.login(request.loginId(), request.password());
        return APISuccessResponse.of(HttpStatus.OK, response);
    }

    @GetMapping("/me")
    public ResponseEntity<APISuccessResponse<AdminInfoResponse>> getAdminInfo(
            @Authentication final AuthCredential authCredential
    ) {
        final AdminInfoResponse response = adminAuthService.getAdminInfo(authCredential.authRole(), authCredential.accountId());
        return APISuccessResponse.of(HttpStatus.OK, response);
    }
}
