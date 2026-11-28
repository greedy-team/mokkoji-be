package com.greedy.mokkoji.api.auth.controller.argumentResolver;

import com.greedy.mokkoji.api.auth.service.AuthTokenResolver;
import com.greedy.mokkoji.api.jwt.JwtUtil;
import com.greedy.mokkoji.common.exception.MokkojiException;
import com.greedy.mokkoji.enums.message.FailMessage;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.util.Optional;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class UserAuthArgumentResolver implements HandlerMethodArgumentResolver {

    private static final Set<String> EXCLUDE_PATTERNS = Set.of("/clubs", "/recruitments", "/comments");
    private final AuthTokenResolver authTokenResolver;
    private final JwtUtil jwtUtil;

    @Override
    public boolean supportsParameter(final MethodParameter parameter) {
        boolean isAuthenticationAnnotation = parameter.hasParameterAnnotation(Authentication.class);
        boolean isAuthCredential = parameter.getParameterType().equals(AuthCredential.class);

        return isAuthenticationAnnotation && isAuthCredential;
    }

    @Override
    public Object resolveArgument(final MethodParameter parameter, final ModelAndViewContainer mavContainer, final NativeWebRequest webRequest, final WebDataBinderFactory binderFactory) throws Exception {
        final HttpServletRequest request = (HttpServletRequest) webRequest.getNativeRequest();
        final String requestURI = request.getRequestURI();
        final Optional<String> token = authTokenResolver.resolveToken(request);

        if (token.isEmpty() && isExcludedPath(requestURI)) {
            return new AuthCredential(null, null);
        }

        return jwtUtil.getCredentialFromToken(
                token.orElseThrow(() -> new MokkojiException(FailMessage.UNAUTHORIZED_EMPTY_HEADER))
        );
    }

    private boolean isExcludedPath(final String requestURI) {
        return EXCLUDE_PATTERNS.stream().anyMatch(requestURI::contains);
    }
}
