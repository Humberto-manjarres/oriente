package com.biblioteca.oriente.infraestructure.driven_adapters.reactive_repository.escritor_mongo;

import com.biblioteca.oriente.domain.model.escritor.Escritor;
import com.biblioteca.oriente.domain.model.escritor.gateway.EscritorGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Repository
public class EscritorAdapter implements EscritorGateway {

    private final EscritorRepository repository;

    private final EscritorTransformerRepository mapper;

    @Override
    public Mono<Escritor> crearEscritor(Escritor escritor) {
        return repository.save(mapper.toEntity(escritor))
                .doOnNext(saved -> System.out.println("GUARDADO EN MONGO: " + saved.getId() + " | " + saved.getIdentificacion()))
                .doOnError(err -> System.err.println("ERROR EN MONGO: " + err.getMessage()))
                .map(mapper::toDomain);
    }

    @Override
    public Mono<Escritor> consultarEscritor(String identificacion) {
        return repository.findByIdentificacion(identificacion)
                .doOnNext(found -> System.out.println("ENCONTRADO EN MONGO: " + found.getId() + " | " + found.getIdentificacion()))
                .doOnError(err -> System.err.println("ERROR EN MONGO: " + err.getMessage()))
                .map(mapper::toDomain);
    }
}
