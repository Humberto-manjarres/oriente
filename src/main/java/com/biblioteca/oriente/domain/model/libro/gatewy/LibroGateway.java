package com.biblioteca.oriente.domain.model.libro.gatewy;

import com.biblioteca.oriente.domain.model.libro.Libro;
import reactor.core.publisher.Mono;

public interface LibroGateway {
    Mono<Libro> crearLibro(Libro libro);
    Mono<Libro> consultarLibro(String idLibro);
}
