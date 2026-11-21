package com.biblioteca.oriente.infraestructure.driven_adapters.reactive_repository.escritor_mongo;

import com.biblioteca.oriente.domain.model.escritor.Escritor;
import org.mapstruct.Mapper;

@Mapper
public interface EscritorTransformerRepository {
    EscritorEntity toEntity(Escritor escritor);
    Escritor toDomain(EscritorEntity escritorEntity);
}
