package com.biblioteca.oriente.domain.model.escritor.gateway;

import com.biblioteca.oriente.domain.model.escritor.Escritor;
import reactor.core.publisher.Mono;

public interface EscritorGateway {
    Mono<Escritor> crearEscritor(Escritor escritor);
    Mono<Escritor> consultarEscritor(String identificacion);
}
