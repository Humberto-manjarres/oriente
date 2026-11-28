package com.biblioteca.oriente.domain.model.libro.gatewy;

import com.biblioteca.oriente.domain.model.libro.Caratula;
import reactor.core.publisher.Mono;

public interface LibroConsumerGateway {
    Mono<Caratula> consultarCaratulaLibro(String idLibro);
}
