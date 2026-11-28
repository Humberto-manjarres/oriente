package com.biblioteca.oriente.infraestructure.driven_adapters.reactive_repository.libro_mongo;

import com.biblioteca.oriente.domain.model.libro.Libro;
import com.biblioteca.oriente.domain.model.libro.gatewy.LibroGateway;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;


@Slf4j
@RequiredArgsConstructor
@Repository
public class LibroAdapter implements LibroGateway {

    private final LibroRepository libroRepository;

    private final LibroTransformerRepository libroTransformerRepository;

    @Override
    public Mono<Libro> crearLibro(Libro libro) {
        return null;
    }

    @Override
    public Mono<Libro> consultarLibro(String idLibro) {
        return null;
    }
}
