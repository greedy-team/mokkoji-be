package com.greedy.mokkoji.api.user.controller;

import com.greedy.mokkoji.api.auth.controller.argumentResolver.AuthCredential;
import com.greedy.mokkoji.api.auth.controller.argumentResolver.Authentication;
import com.greedy.mokkoji.api.auth.service.AuthCookieFactory;
import com.greedy.mokkoji.api.auth.service.AuthTokenResolver;
import com.greedy.mokkoji.api.jwt.BearerAuthExtractor;
import com.greedy.mokkoji.api.user.dto.request.UpdateUserInformationRequest;
import com.greedy.mokkoji.api.user.dto.request.kakao.KakaoSocialLoginRequest;
import com.greedy.mokkoji.api.user.dto.resopnse.*;
import com.greedy.mokkoji.api.user.service.UserService;
import com.greedy.mokkoji.api.user.service.kakao.KakaoRedirectUriResolver;
import com.greedy.mokkoji.common.exception.MokkojiException;
import com.greedy.mokkoji.common.response.APISuccessResponse;
import com.greedy.mokkoji.enums.message.FailMessage;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("${api.prefix}/users")
public class UserController implements UserControllerSwagger {

    private final BearerAuthExtractor bearerAuthExtractor;
    private final AuthCookieFactory authCookieFactory;
    private final AuthTokenResolver authTokenResolver;
    private final UserService userService;
    private final KakaoRedirectUriResolver kakaoRedirectUriResolver;

    @PostMapping("/auth/kakao")
    public ResponseEntity<APISuccessResponse<LoginResponse>> kakaoLogin(
            @RequestHeader(value = HttpHeaders.ORIGIN, required = false) final String origin,
            @RequestBody final KakaoSocialLoginRequest request,
            final HttpServletResponse response
    ) {
        final String redirectUri = kakaoRedirectUriResolver.resolve(origin);
        final LoginResponse loginResponse = userService.kakaoLogin(request.code(), redirectUri, request.universityCode());

        addCookie(response, authCookieFactory.accessToken(loginResponse.accessToken()));
        addCookie(response, authCookieFactory.refreshToken(loginResponse.refreshToken()));

        return APISuccessResponse.of(HttpStatus.OK, loginResponse);
    }

    @PostMapping("/auth/refresh")
    public ResponseEntity<APISuccessResponse<RefreshResponse>> refresh(
            @RequestHeader(value = "Authorization", required = false) final String bearerToken,
            final HttpServletRequest request,
            final HttpServletResponse response
    ) {
        final String refreshToken = resolveRefreshToken(bearerToken, request);
        final String newAccessToken = userService.refreshAccessToken(refreshToken);

        addCookie(response, authCookieFactory.accessToken(newAccessToken));

        RefreshResponse refreshResponse = RefreshResponse.of(newAccessToken);
        return APISuccessResponse.of(HttpStatus.OK, refreshResponse);
    }

    private String resolveRefreshToken(final String bearerToken, final HttpServletRequest request) {
        if (bearerToken != null) {
            return bearerAuthExtractor.extractTokenValue(bearerToken);
        }
        return authTokenResolver.resolveRefreshToken(request)
                .orElseThrow(() -> new MokkojiException(FailMessage.UNAUTHORIZED_EMPTY_HEADER));
    }

    private void addCookie(final HttpServletResponse response, final ResponseCookie cookie) {
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    @PostMapping("/auth/logout")
    public ResponseEntity<APISuccessResponse<Void>> logout(
            @Authentication AuthCredential authCredential,
            final HttpServletResponse response
    ) {
        userService.logout(authCredential.authRole(), authCredential.accountId());

        addCookie(response, authCookieFactory.expiredAccessToken());
        addCookie(response, authCookieFactory.expiredRefreshToken());

        return APISuccessResponse.of(HttpStatus.OK, null);
    }

    @GetMapping
    public ResponseEntity<APISuccessResponse<UserInformationResponse>> getUserInformation(
            @Authentication AuthCredential authCredential
    ) {
        final Long userId = authCredential.accountId();
        final UserInformationResponse userInformationResponse = userService.getUserInformation(userId);
        return APISuccessResponse.of(HttpStatus.OK, userInformationResponse);
    }

    @PatchMapping
    public ResponseEntity<APISuccessResponse<Void>> updateUserInformation(
            @Authentication AuthCredential authCredential,
            @RequestBody @Valid UpdateUserInformationRequest updateUserInformationRequest
    ) {
        final Long userId = authCredential.accountId();
        userService.updateUserInformation(
                userId,
                updateUserInformationRequest.name(),
                updateUserInformationRequest.email(),
                updateUserInformationRequest.isEmailOn(),
                updateUserInformationRequest.universityCode()
        );
        return APISuccessResponse.of(HttpStatus.OK, null);
    }

    @DeleteMapping
    public ResponseEntity<APISuccessResponse<Void>> deleteUser(
            @Authentication AuthCredential authCredential
    ) {
        userService.deleteUser(authCredential.authRole(), authCredential.accountId());
        return APISuccessResponse.of(HttpStatus.OK, null);
    }

    @GetMapping("/roles")
    public ResponseEntity<APISuccessResponse<UserRoleResponse>> getUserRole(
            @Authentication AuthCredential authCredential
    ) {
        return APISuccessResponse.of(HttpStatus.OK, userService.getUserRole(authCredential.accountId()));
    }

    @GetMapping("/manage/clubs")
    public ResponseEntity<APISuccessResponse<UserManageClubsResponse>> getUserManageClubs(
            @Authentication AuthCredential authCredential
    ) {
        return APISuccessResponse.of(HttpStatus.OK, userService.getUserManageClubs(authCredential.accountId()));
    }
}
