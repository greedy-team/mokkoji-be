package com.greedy.mokkoji.api.user.controller;

import com.greedy.mokkoji.api.auth.controller.argumentResolver.AuthCredential;
import com.greedy.mokkoji.api.auth.controller.argumentResolver.Authentication;
import com.greedy.mokkoji.api.jwt.BearerAuthExtractor;
import com.greedy.mokkoji.api.user.dto.request.LoginRequest;
import com.greedy.mokkoji.api.user.dto.request.UpdateUserInformationRequest;
import com.greedy.mokkoji.api.user.dto.resopnse.*;
import com.greedy.mokkoji.api.user.service.TokenService;
import com.greedy.mokkoji.api.user.service.UserService;
import com.greedy.mokkoji.common.response.APISuccessResponse;
import com.greedy.mokkoji.db.user.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("${api.prefix}/users")
public class UserController {

    private final BearerAuthExtractor bearerAuthExtractor;
    private final UserService userService;
    private final TokenService tokenService;

    @Operation(
            summary = "로그인 API",
            description = "학번과 비밀번호를 통해 로그인을 수행합니다."
    )
    @ApiResponse(responseCode = "200", description = "로그인 성공")
    @PostMapping("/auth/login")
    public ResponseEntity<APISuccessResponse<LoginResponse>> login(@RequestBody LoginRequest request) {
        final User user = userService.login(request.studentId(), request.password());
        final LoginResponse loginResponse = tokenService.generateToken(user.getId());

        return APISuccessResponse.of(HttpStatus.OK, loginResponse);
    }

    @Operation(
            summary = "Access Token 재발급 API",
            description = "Refresh Token을 사용해 새로운 Access Token을 발급받습니다."
    )
    @ApiResponse(responseCode = "200", description = "Access Token 재발급 성공")
    @Parameter(
            name = "Authorization",
            description = "Bearer + Refresh Token",
            in = ParameterIn.HEADER,
            required = true
    )
    @PostMapping("/auth/refresh")
    public ResponseEntity<APISuccessResponse<RefreshResponse>> refresh(
            @RequestHeader("Authorization") String bearerToken
    ) {
        final String refreshToken = bearerAuthExtractor.extractTokenValue(bearerToken);
        final String newAccessToken = userService.refreshAccessToken(refreshToken);
        RefreshResponse refreshResponse = RefreshResponse.of(newAccessToken);

        return APISuccessResponse.of(HttpStatus.OK, refreshResponse);
    }

    @Operation(
            summary = "로그아웃 API",
            description = "현재 로그인된 사용자를 로그아웃합니다.",
            security = {@SecurityRequirement(name = "JWT")}
    )
    @ApiResponse(responseCode = "200", description = "로그아웃 성공")
    @PostMapping("/auth/logout")
    public ResponseEntity<APISuccessResponse<Void>> logout(
            @Authentication AuthCredential authCredential
    ) {
        final Long userId = authCredential.userId();
        userService.logOut(userId);
        return APISuccessResponse.of(HttpStatus.OK, null);
    }

    @Operation(
            summary = "사용자 정보 조회 API",
            description = "현재 로그인된 사용자의 정보를 조회합니다.",
            security = {@SecurityRequirement(name = "JWT")}
    )
    @ApiResponse(responseCode = "200", description = "사용자 정보 조회 성공")
    @GetMapping
    public ResponseEntity<APISuccessResponse<UserInformationResponse>> getUserInformation(
            @Authentication AuthCredential authCredential
    ) {
        final Long userId = authCredential.userId();
        final User user = userService.findUser(userId);
        final UserInformationResponse userInformationResponse = UserInformationResponse.of(user);

        return APISuccessResponse.of(HttpStatus.OK, userInformationResponse);
    }

    @Operation(
            summary = "사용자 정보 수정 API",
            description = "현재 로그인된 사용자의 이메일을 수정합니다.",
            security = {@SecurityRequirement(name = "JWT")}
    )
    @ApiResponse(responseCode = "200", description = "사용자 정보 수정 성공")
    @PutMapping
    public ResponseEntity<APISuccessResponse<Void>> updateUserInformation(
            @Authentication AuthCredential authCredential,
            @RequestBody UpdateUserInformationRequest updateUserInformationRequest
    ) {
        final Long userId = authCredential.userId();
        userService.updateEmail(userId, updateUserInformationRequest.email());

        return APISuccessResponse.of(HttpStatus.OK, null);
    }

    @Operation(
            summary = "사용자 권한 조회 API",
            description = "현재 로그인된 사용자의 권한 정보를 조회합니다.",
            security = {@SecurityRequirement(name = "JWT")}
    )
    @ApiResponse(responseCode = "200", description = "권한 조회 성공")
    @GetMapping("/roles")
    public ResponseEntity<APISuccessResponse<UserRoleResponse>> getUserRole(
            @Authentication AuthCredential authCredential
    ) {
        return APISuccessResponse.of(HttpStatus.OK, userService.getUserRole(authCredential.userId()));
    }

    @Operation(
            summary = "관리 중인 동아리 조회 API",
            description = "현재 로그인된 사용자가 관리 중인 동아리 목록을 조회합니다.",
            security = {@SecurityRequirement(name = "JWT")}
    )
    @ApiResponse(responseCode = "200", description = "관리 동아리 조회 성공")
    @GetMapping("/manage/clubs")
    public ResponseEntity<APISuccessResponse<UserManageClubsResponse>> getUserManageClubs(
            @Authentication AuthCredential authCredential
    ) {
        return APISuccessResponse.of(HttpStatus.OK, userService.getUserManageClubs(authCredential.userId()));
    }
}
