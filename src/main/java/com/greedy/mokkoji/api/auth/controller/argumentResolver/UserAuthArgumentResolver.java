package com.greedy.mokkoji.api.auth.controller.argumentResolver;

import com.greedy.mokkoji.api.jwt.BearerAuthExtractor;
import com.greedy.mokkoji.api.jwt.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.util.Set;

@Component
@RequiredArgsConstructor
public class UserAuthArgumentResolver implements HandlerMethodArgumentResolver {

    private static final Set<String> EXCLUDE_PATTERNS = Set.of("/clubs");
    private final BearerAuthExtractor bearerAuthExtractor;
    private final JwtUtil jwtUtil;

    @Override
    public boolean supportsParameter(final MethodParameter parameter) {
        boolean isAuthenticationAnnotation = parameter.hasParameterAnnotation(Authentication.class);
        boolean isAuthCredential = parameter.getParameterType().equals(AuthCredential.class);

        return isAuthenticationAnnotation && isAuthCredential;
    }

    @Override
    public Object resolveArgument(
            final MethodParameter parameter,
            final ModelAndViewContainer mavContainer,
            final NativeWebRequest webRequest,
            final WebDataBinderFactory binderFactory
    ) throws Exception {
        final HttpServletRequest request = (HttpServletRequest) webRequest.getNativeRequest();
        final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        final String requestURI = request.getRequestURI();

        if (isExcludedRequest(requestURI, authHeader)) {
            return new AuthCredential(null);
        }

        final String token = bearerAuthExtractor.extractTokenValue(authHeader);
        final Long userId = jwtUtil.getUserIdFromToken(token);
        return new AuthCredential(userId);
    }

    private boolean isExcludedRequest(String requestURI, String authHeader) {
        return EXCLUDE_PATTERNS.stream().anyMatch(requestURI::contains) && authHeader == null;
    }
}
