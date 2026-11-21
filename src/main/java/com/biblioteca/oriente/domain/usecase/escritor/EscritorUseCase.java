package com.biblioteca.oriente.domain.usecase.escritor;

import com.biblioteca.oriente.domain.model.escritor.Escritor;
import com.biblioteca.oriente.domain.model.escritor.gateway.EscritorGateway;
import com.biblioteca.oriente.domain.model.exception.RecursoNoEncontradoException;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class EscritorUseCase {

    private final EscritorGateway escritorGateway;

    public Mono<Escritor> crearEscritor(Escritor escritor){
        return escritorGateway.crearEscritor(escritor);
    }

    public Mono<Escritor> obtenerEscritorPorId(String escritorId) {
        return escritorGateway.consultarEscritor(escritorId)
                .switchIfEmpty(Mono.error(new RecursoNoEncontradoException("Escritor", escritorId)));
    }

}
