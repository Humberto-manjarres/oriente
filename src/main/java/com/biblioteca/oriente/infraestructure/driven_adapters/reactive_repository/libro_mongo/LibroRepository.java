package com.biblioteca.oriente.infraestructure.driven_adapters.reactive_repository.libro_mongo;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface LibroRepository extends ReactiveMongoRepository<LibroEntity, String> {
}
