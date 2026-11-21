package com.biblioteca.oriente.infraestructure.entry_points.reactive_web.exception;

import com.biblioteca.oriente.domain.model.exception.RecursoNoEncontradoException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Instant;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleNotFound(
            RecursoNoEncontradoException ex,
            ServerWebExchange exchange) {

        ErrorResponse body = new ErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                HttpStatus.NOT_FOUND.getReasonPhrase(),
                ex.getMessage(),
                exchange.getRequest().getPath().value(),
                Instant.now()
        );
        return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).body(body));
    }

    @ExceptionHandler(Exception.class)
    public Mono<ResponseEntity<ErrorResponse>> handleGeneric(
            Exception ex,
            ServerWebExchange exchange) {

        ErrorResponse body = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
                "Ocurrió un error interno en el servidor",
                exchange.getRequest().getPath().value(),
                Instant.now()
        );
        return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body));
    }


//    @ExceptionHandler(RecursoDuplicadoException.class)
//    public Mono<ResponseEntity<ErrorResponse>> handleConflict(
//            RecursoDuplicadoException ex,
//            ServerWebExchange exchange) {
//
//        ErrorResponse body = new ErrorResponse(
//                HttpStatus.CONFLICT.value(),
//                HttpStatus.CONFLICT.getReasonPhrase(),
//                ex.getMessage(),
//                exchange.getRequest().getPath().value(),
//                Instant.now()
//        );
//        return Mono.just(ResponseEntity.status(HttpStatus.CONFLICT).body(body));
//    }

}
