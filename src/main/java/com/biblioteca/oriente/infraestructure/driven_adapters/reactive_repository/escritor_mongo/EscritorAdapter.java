package com.biblioteca.oriente.infraestructure.driven_adapters.reactive_repository.escritor_mongo;

import com.biblioteca.oriente.domain.model.escritor.Escritor;
import com.biblioteca.oriente.domain.model.escritor.gateway.EscritorGateway;
import com.biblioteca.oriente.domain.model.exception.RecursoDuplicadoException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Slf4j
@RequiredArgsConstructor
@Repository
public class EscritorAdapter implements EscritorGateway {

    private final EscritorRepository repository;

    private final EscritorTransformerRepository mapper;

    /**
     * Usa insert (no save) para que nunca sobrescriba un documento existente. Si dos peticiones
     * simultáneas pasan la validación del caso de uso, el índice único sobre identificacion
     * rechaza la segunda y se traduce a la excepción de dominio.
     */
    @Override
    public Mono<Escritor> crearEscritor(Escritor escritor) {
        return Mono.fromSupplier(() -> mapper.toEntity(escritor))
                .flatMap(repository::insert)
                .doOnNext(guardado -> log.debug("Escritor guardado: id={} identificacion={}",
                        guardado.getId(), guardado.getIdentificacion()))
                .onErrorMap(DuplicateKeyException.class,
                        error -> new RecursoDuplicadoException("Escritor", escritor.getIdentificacion()))
                .map(mapper::toDomain);
    }

    @Override
    public Mono<Escritor> consultarEscritor(String identificacion) {
        return repository.findByIdentificacion(identificacion)
                .map(mapper::toDomain);
    }
}
