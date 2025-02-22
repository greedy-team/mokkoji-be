package com.greedy.mokkoji.api.auth.controller.argumentResolver;

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

@Component
@RequiredArgsConstructor
public class UserAuthArgumentResolver implements HandlerMethodArgumentResolver {
    private final JwtUtil jwtUtil;

    @Override
    public boolean supportsParameter(final MethodParameter parameter) {
        boolean isAuthenticationAnnotation = parameter.hasParameterAnnotation(Authentication.class);
        boolean isAuthCredential = parameter.getParameterType().equals(Authentication.class);

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
        Long userId = jwtUtil.getUserIdFromToken(request.getHeader(HttpHeaders.AUTHORIZATION));
        return new AuthCredential(userId);
    }
}
