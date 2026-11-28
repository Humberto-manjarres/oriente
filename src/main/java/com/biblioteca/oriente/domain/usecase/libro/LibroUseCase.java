package com.biblioteca.oriente.domain.usecase.libro;

import com.biblioteca.oriente.domain.model.libro.Libro;
import com.biblioteca.oriente.domain.model.libro.gatewy.LibroGateway;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class LibroUseCase {

    private final LibroGateway libroGateway;

    public Mono<Libro> crearLibro(Libro libro) {
        return Mono.just(libro);
    }

    public Mono<Libro> consultarLibro(String idLibro) {
        return Mono.empty();
    }
}
