package com.nnson128.apigateway.config;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Optional;

@Component
public class CustomAuthFilter implements GlobalFilter, Ordered {

@Override
public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
    return ReactiveSecurityContextHolder.getContext()
            .map(securityContext -> securityContext.getAuthentication())
            .filter(Authentication::isAuthenticated)
            .flatMap(authentication -> {
                if (authentication.getPrincipal() instanceof Jwt) {
                    Jwt jwt = (Jwt) authentication.getPrincipal();
                    String userId = Optional.ofNullable(jwt.getClaimAsString("sub"))
                            .orElseThrow(() -> new IllegalArgumentException("User ID not found in JWT claims"));

                    // Cách đúng đắn để thêm header mà không làm mất các header khác
                    ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                            .header("X-User-Id", userId) // Chỉ thêm hoặc ghi đè X-User-Id
                            .build();

                    return chain.filter(exchange.mutate().request(mutatedRequest).build());
                }
                return chain.filter(exchange);
            })
            .switchIfEmpty(chain.filter(exchange));
}

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 1;
    }
}