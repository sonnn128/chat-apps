package com.example.apigateway.service;

import com.example.apigateway.dto.IntrospectRequest;
import com.example.apigateway.dto.IntrospectResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class IdentityService {
    private final WebClient.Builder webClientBuilder;

    public Mono<IntrospectResponse> introspect(String token) {
        WebClient webClient = webClientBuilder.baseUrl("http://auth-service/api/auth").build();

        return webClient.post()
                .uri("/introspect")
                .bodyValue(new IntrospectRequest(token))
                .retrieve()
                .bodyToMono(IntrospectResponse.class)
                .onErrorResume(throwable -> {
                    return Mono.just(new IntrospectResponse(false, null, null));
                });
    }
}
