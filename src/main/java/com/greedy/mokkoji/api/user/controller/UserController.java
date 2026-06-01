package com.greedy.mokkoji.api.user.controller;

import com.greedy.mokkoji.api.auth.controller.argumentResolver.AuthCredential;
import com.greedy.mokkoji.api.auth.controller.argumentResolver.Authentication;
import com.greedy.mokkoji.api.jwt.BearerAuthExtractor;
import com.greedy.mokkoji.api.user.dto.request.UpdateUserInformationRequest;
import com.greedy.mokkoji.api.user.dto.request.kakao.KakaoSocialLoginRequest;
import com.greedy.mokkoji.api.user.dto.resopnse.*;
import com.greedy.mokkoji.api.user.service.UserService;
import com.greedy.mokkoji.common.response.APISuccessResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("${api.prefix}/users")
public class UserController implements UserControllerSwagger {

    private final BearerAuthExtractor bearerAuthExtractor;
    private final UserService userService;

    @PostMapping("/auth/kakao")
    public ResponseEntity<APISuccessResponse<LoginResponse>> kakaoLogin(
            @RequestBody final KakaoSocialLoginRequest request
    ) {
        final LoginResponse loginResponse = userService.kakaoLogin(request.code());
        return APISuccessResponse.of(HttpStatus.OK, loginResponse);
    }

    @PostMapping("/auth/refresh")
    public ResponseEntity<APISuccessResponse<RefreshResponse>> refresh(
            @RequestHeader("Authorization") String bearerToken
    ) {
        final String refreshToken = bearerAuthExtractor.extractTokenValue(bearerToken);
        final String newAccessToken = userService.refreshAccessToken(refreshToken);
        RefreshResponse refreshResponse = RefreshResponse.of(newAccessToken);
        return APISuccessResponse.of(HttpStatus.OK, refreshResponse);
    }

    @PostMapping("/auth/logout")
    public ResponseEntity<APISuccessResponse<Void>> logout(
            @Authentication AuthCredential authCredential
    ) {
        userService.logout(authCredential.authRole(), authCredential.userId());
        return APISuccessResponse.of(HttpStatus.OK, null);
    }

    @GetMapping
    public ResponseEntity<APISuccessResponse<UserInformationResponse>> getUserInformation(
            @Authentication AuthCredential authCredential
    ) {
        final Long userId = authCredential.userId();
        final UserInformationResponse userInformationResponse = userService.getUserInformation(userId);
        return APISuccessResponse.of(HttpStatus.OK, userInformationResponse);
    }

    @PatchMapping
    public ResponseEntity<APISuccessResponse<Void>> updateUserInformation(
            @Authentication AuthCredential authCredential,
            @RequestBody @Valid UpdateUserInformationRequest updateUserInformationRequest
    ) {
        final Long userId = authCredential.userId();
        userService.updateUserInformation(
                userId,
                updateUserInformationRequest.name(),
                updateUserInformationRequest.email(),
                updateUserInformationRequest.isEmailOn(),
                updateUserInformationRequest.universityCode()
        );
        return APISuccessResponse.of(HttpStatus.OK, null);
    }

    @GetMapping("/roles")
    public ResponseEntity<APISuccessResponse<UserRoleResponse>> getUserRole(
            @Authentication AuthCredential authCredential
    ) {
        return APISuccessResponse.of(HttpStatus.OK, userService.getUserRole(authCredential.userId()));
    }

    @GetMapping("/manage/clubs")
    public ResponseEntity<APISuccessResponse<UserManageClubsResponse>> getUserManageClubs(
            @Authentication AuthCredential authCredential
    ) {
        return APISuccessResponse.of(HttpStatus.OK, userService.getUserManageClubs(authCredential.userId()));
    }
}
