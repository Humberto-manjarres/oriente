package com.biblioteca.oriente.infraestructure.driven_adapters.reactive_web.base;

import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

public abstract class Consumer {

    protected final WebClient webClient;

    protected Consumer(String baseUrl) {
        this.webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    protected <R> Mono<R> getRequest(
            String uri,
            Class<R> responseClass) {

        return webClient
                .get()
                .uri(uri)
                .retrieve()
                .bodyToMono(responseClass);
    }

    protected <B, R> Mono<R> postRequest(
            String uri,
            B body,
            Class<R> responseClass) {

        return webClient
                .post()
                .uri(uri)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(responseClass);
    }

    public abstract void configureHeaders(HttpHeaders headers);
}