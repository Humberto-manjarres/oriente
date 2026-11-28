package com.biblioteca.oriente.infraestructure.driven_adapters.reactive_repository.libro_mongo;

import com.biblioteca.oriente.domain.model.libro.Libro;
import org.mapstruct.Mapper;

@Mapper
public interface LibroTransformerRepository {
    LibroEntity toEntity(Libro libro);
    Libro toDomain(LibroEntity libroEntity);
}
