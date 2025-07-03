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
                .bodyToMono(IntrospectResponse.class)
                .onErrorResume(throwable -> {
                    log.error("Introspection call failed: {}", throwable.getMessage());
                    return Mono.just(new IntrospectResponse(false, null, null, null));
                });
    }
}