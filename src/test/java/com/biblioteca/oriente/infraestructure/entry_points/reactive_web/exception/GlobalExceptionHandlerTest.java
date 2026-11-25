package com.biblioteca.oriente.infraestructure.entry_points.reactive_web.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ResponseStatusException;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    private final MockServerWebExchange exchange =
            MockServerWebExchange.from(MockServerHttpRequest.get("/escritor/1001"));

    @Test
    void handleResponseStatusDebeConservarCodigoEstandar() {
        StepVerifier.create(handler.handleResponseStatus(
                        new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE), exchange))
                .assertNext(response -> {
                    assertThat(response.getStatusCode().value()).isEqualTo(406);
                    assertThat(response.getBody()).isNotNull();
                    assertThat(response.getBody().error()).isEqualTo("Not Acceptable");
                    assertThat(response.getBody().message()).isEqualTo("Not Acceptable");
                    assertThat(response.getBody().path()).isEqualTo("/escritor/1001");
                })
                .verifyComplete();
    }

    @Test
    void handleResponseStatusDebeUsarElNumeroCuandoElCodigoNoEsEstandar() {
        StepVerifier.create(handler.handleResponseStatus(
                        new ResponseStatusException(HttpStatusCode.valueOf(499)), exchange))
                .assertNext(response -> {
                    assertThat(response.getStatusCode().value()).isEqualTo(499);
                    assertThat(response.getBody()).isNotNull();
                    assertThat(response.getBody().status()).isEqualTo(499);
                    assertThat(response.getBody().error()).isEqualTo("499");
                })
                .verifyComplete();
    }

    @Test
    void handleGenericNoDebeExponerElMensajeTecnico() {
        StepVerifier.create(handler.handleGeneric(new IllegalStateException("detalle técnico interno"), exchange))
                .assertNext(response -> {
                    assertThat(response.getStatusCode().value()).isEqualTo(500);
                    assertThat(response.getBody()).isNotNull();
                    assertThat(response.getBody().message()).isEqualTo(GlobalExceptionHandler.MENSAJE_ERROR_INTERNO);
                })
                .verifyComplete();
    }
}
