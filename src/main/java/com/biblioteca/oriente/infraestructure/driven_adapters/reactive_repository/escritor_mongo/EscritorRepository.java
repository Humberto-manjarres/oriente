package com.biblioteca.oriente.infraestructure.driven_adapters.reactive_repository.escritor_mongo;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

public interface EscritorRepository extends ReactiveMongoRepository<EscritorEntity, String> {
    Mono<EscritorEntity> findByIdentificacion(String identificacion);
}
