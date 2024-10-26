package com.eatpizzaquickly.apigateway.common.config;


import com.eatpizzaquickly.apigateway.common.dto.CustomUserDetails;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import com.eatpizzaquickly.apigateway.common.dto.CustomUserDetails;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.apache.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtFilter implements WebFilter {
    private final JwtUtils jwtUtil;
    private final RouterValidator routerValidator;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        var request = exchange.getRequest();
        String path = request.getURI().getPath();
        log.info("현재 요청 경로: {}", path);
        log.info("isSecured 체크 결과: {}", routerValidator.isSecured.test(request));

        // 로그인 경로는 JWT 검사를 우회
        if (path.equals("/api/v1/users/login")) {
            return chain.filter(exchange);
        }

        if (routerValidator.isSecured.test(request)) {
            if (!request.getHeaders().containsKey("Authorization")) {
                return onError(exchange, "No Authorization Header", HttpStatus.UNAUTHORIZED);
            }

            // Bearer 접두사 제거
            String authorizationHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
            String token = jwtUtil.substringToken(authorizationHeader);  // Bearer 제거
            Claims claims = jwtUtil.extractClaims(token);  // 토큰 검증

            if (claims == null) {
                return onError(exchange, "Invalid Jwt Token", HttpStatus.UNAUTHORIZED);
            }

            Long userId = Long.valueOf(claims.getSubject());
            String userRole = claims.get("userRole", String.class);

            UserDetails userDetails = CustomUserDetails.builder()
                    .userId(userId)
                    .role(userRole)
                    .build();
            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities()
            );

            return chain.filter(exchange.mutate()
                            .request(exchange.getRequest().mutate()
                                    .header("X-Authenticated-User", String.valueOf(userId))
                                    .build())
                            .build())
                    .contextWrite(ReactiveSecurityContextHolder.withAuthentication(auth));
        }

        return chain.filter(exchange);
    }

    private Mono<Void> onError(ServerWebExchange exchange, String error, HttpStatus status) {
        var response = exchange.getResponse();
        response.setStatusCode(status);
        return response.setComplete();
    }
}