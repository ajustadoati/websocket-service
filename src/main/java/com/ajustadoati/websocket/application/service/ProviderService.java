package com.ajustadoati.websocket.application.service;

import com.ajustadoati.websocket.application.service.dto.ProviderDto;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class ProviderService {

    private final WebClient webClient;

    public ProviderService(WebClient.Builder builder) {
        this.webClient = builder.baseUrl("https://qr.ajustadoati.com/api").build();
    }

    public Mono<ProviderDto> findByUsername(String username) {
        return webClient.get()
            .uri("/users/{username}/username", username)
            .retrieve()
            .bodyToMono(ProviderDto.class)
            .onErrorResume(e -> {
                // log and return empty if needed
                return Mono.empty();
            });
    }
}
