package com.example.apigateway.service;

import com.example.apigateway.dto.IntrospectRequest;
import com.example.apigateway.dto.IntrospectResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class IdentityService {
    private final WebClient.Builder webClientBuilder;

    public Mono<IntrospectResponse> introspect(String token) {
        return webClientBuilder.build().post()
                .uri("http://auth-service/api/v1/auth/introspect")
                .bodyValue(new IntrospectRequest(token))
                .retrieve()
                .onStatus(
                        status -> status.isError(), // Kiểm tra nếu là lỗi 4xx hoặc 5xx
                        clientResponse -> clientResponse.bodyToMono(String.class)
                                .flatMap(errorBody -> {
                                    log.error("Error response from auth-service. Status: {}, Body: {}", clientResponse.statusCode(), errorBody);
                                    return Mono.error(new RuntimeException("Introspection failed with status: " + clientResponse.statusCode()));
                                })
                )
                .bodyToMono(IntrospectResponse.class)
                .doOnSuccess(response -> {
                    log.info("Gateway received successful introspection response: {}", response);
                })
                .onErrorResume(throwable -> {
                    log.error("Error in WebClient call to introspect token", throwable);
                    return Mono.just(new IntrospectResponse(false, null, null, null));
                });
    }
}