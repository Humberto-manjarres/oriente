package com.biblioteca.oriente.infraestructure.driven_adapters.reactive_repository.escritor_mongo;

import com.biblioteca.oriente.domain.model.escritor.Escritor;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.assertj.core.api.Assertions.assertThat;

class EscritorTransformerRepositoryTest {

    private final EscritorTransformerRepository mapper = Mappers.getMapper(EscritorTransformerRepository.class);

    @Test
    void toEntityDebeCopiarTodosLosCampos() {
        Escritor escritor = new Escritor("abc123", "1001", "Gabriel García Márquez", "Colombiana");

        EscritorEntity entity = mapper.toEntity(escritor);

        assertThat(entity.getId()).isEqualTo("abc123");
        assertThat(entity.getIdentificacion()).isEqualTo("1001");
        assertThat(entity.getNombre()).isEqualTo("Gabriel García Márquez");
        assertThat(entity.getNacionalidad()).isEqualTo("Colombiana");
    }

    @Test
    void toDomainDebeCopiarTodosLosCampos() {
        EscritorEntity entity = new EscritorEntity("abc123", "1001", "Gabriel García Márquez", "Colombiana");

        Escritor escritor = mapper.toDomain(entity);

        assertThat(escritor).isEqualTo(new Escritor("abc123", "1001", "Gabriel García Márquez", "Colombiana"));
    }

    @Test
    void debeRetornarNullCuandoLaEntradaEsNull() {
        assertThat(mapper.toEntity(null)).isNull();
        assertThat(mapper.toDomain(null)).isNull();
    }
}
