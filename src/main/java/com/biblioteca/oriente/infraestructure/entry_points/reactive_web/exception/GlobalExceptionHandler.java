package com.biblioteca.oriente.infraestructure.entry_points.reactive_web.exception;

import com.biblioteca.oriente.domain.model.exception.CampoObligatorioException;
import com.biblioteca.oriente.domain.model.exception.RecursoDuplicadoException;
import com.biblioteca.oriente.domain.model.exception.RecursoNoEncontradoException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Mono;

import java.time.Instant;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    static final String MENSAJE_SOLICITUD_INVALIDA =
            "La solicitud es inválida: el cuerpo está vacío, no es un JSON válido o tiene parámetros incorrectos";
    static final String MENSAJE_ERROR_INTERNO = "Ocurrió un error interno en el servidor";

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleNotFound(
            RecursoNoEncontradoException ex,
            ServerWebExchange exchange) {

        log.warn("Recurso no encontrado: {}", ex.getMessage());
        return responder(HttpStatus.NOT_FOUND, ex.getMessage(), exchange);
    }

    /**
     * Body ausente, JSON inválido o parámetro con tipo incorrecto: es un error del cliente (400).
     */
    @ExceptionHandler(ServerWebInputException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleBadRequest(
            ServerWebInputException ex,
            ServerWebExchange exchange) {

        log.warn("Solicitud inválida en {}: {}", exchange.getRequest().getPath().value(), ex.getMessage());
        return responder(HttpStatus.BAD_REQUEST, MENSAJE_SOLICITUD_INVALIDA, exchange);
    }

    /**
     * Errores HTTP propios de WebFlux (405 método no permitido, 415 tipo de contenido no soportado,
     * 406 no aceptable, etc.): se conserva su código en lugar de convertirlos en 500.
     */
    @ExceptionHandler(ResponseStatusException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleResponseStatus(
            ResponseStatusException ex,
            ServerWebExchange exchange) {

        HttpStatusCode status = ex.getStatusCode();
        log.warn("Error HTTP {} en {}: {}", status.value(), exchange.getRequest().getPath().value(), ex.getMessage());
        return responder(status, razon(status), exchange);
    }

    @ExceptionHandler(Exception.class)
    public Mono<ResponseEntity<ErrorResponse>> handleGeneric(
            Exception ex,
            ServerWebExchange exchange) {

        log.error("Error no controlado en {}", exchange.getRequest().getPath().value(), ex);
        return responder(HttpStatus.INTERNAL_SERVER_ERROR, MENSAJE_ERROR_INTERNO, exchange);
    }

    @ExceptionHandler(CampoObligatorioException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleCampoObligatorio(
            CampoObligatorioException ex,
            ServerWebExchange exchange) {

        log.warn("Campos obligatorios faltantes: {}", ex.getMessage());
        return responder(HttpStatus.BAD_REQUEST, ex.getMessage(), exchange);
    }

    @ExceptionHandler(RecursoDuplicadoException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleConflict(
            RecursoDuplicadoException ex,
            ServerWebExchange exchange) {

        log.warn("Recurso duplicado: {}", ex.getMessage());
        return responder(HttpStatus.CONFLICT, ex.getMessage(), exchange);
    }

    private static Mono<ResponseEntity<ErrorResponse>> responder(
            HttpStatusCode status,
            String mensaje,
            ServerWebExchange exchange) {

        ErrorResponse body = new ErrorResponse(
                status.value(),
                razon(status),
                mensaje,
                exchange.getRequest().getPath().value(),
                Instant.now()
        );
        return Mono.just(ResponseEntity.status(status).body(body));
    }

    /** Texto estándar del código HTTP; para códigos no estándar devuelve el número. */
    private static String razon(HttpStatusCode status) {
        HttpStatus httpStatus = HttpStatus.resolve(status.value());
        return httpStatus != null ? httpStatus.getReasonPhrase() : String.valueOf(status.value());
    }
}
