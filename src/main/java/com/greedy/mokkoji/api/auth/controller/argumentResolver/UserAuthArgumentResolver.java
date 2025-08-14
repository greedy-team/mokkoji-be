package com.greedy.mokkoji.api.auth.controller.argumentResolver;

import com.greedy.mokkoji.api.jwt.AuthExtractor;
import com.greedy.mokkoji.api.jwt.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Component
@RequiredArgsConstructor
public class UserAuthArgumentResolver implements HandlerMethodArgumentResolver {

    private static final Set<String> EXCLUDE_PATTERNS = Set.of("/clubs", "/recruitments", "/comments");
    private final JwtUtil jwtUtil;
    private final AuthExtractor authExtractor;

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
        final String requestURI = request.getRequestURI();

        final String token = authExtractor.extractAccessToken(request);

        if (isExcludedRequest(requestURI, token)) {
            return new AuthCredential(null);
        }

        final Long userId = jwtUtil.getUserIdFromToken(token);
        return new AuthCredential(userId);
    }

    private boolean isExcludedRequest(String requestURI, String authHeader) {
        return EXCLUDE_PATTERNS.stream().anyMatch(requestURI::contains) && authHeader == null;
    }
}
