package com.biblioteca.oriente.infraestructure.entry_points.reactive_web.escritor;

import com.biblioteca.oriente.domain.model.escritor.Escritor;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.assertj.core.api.Assertions.assertThat;

class EscritorTransformerTest {

    private final EscritorTransformer transformer = Mappers.getMapper(EscritorTransformer.class);

    @Test
    void toEntityDebeConvertirDtoADominio() {
        EscritorDTO dto = new EscritorDTO("abc123", "1001", "Gabriel García Márquez", "Colombiana");

        Escritor escritor = transformer.toEntity(dto);

        assertThat(escritor).isEqualTo(new Escritor("abc123", "1001", "Gabriel García Márquez", "Colombiana"));
    }

    @Test
    void toDtoDebeConvertirDominioADto() {
        Escritor escritor = new Escritor("abc123", "1001", "Gabriel García Márquez", "Colombiana");

        EscritorDTO dto = transformer.toDTO(escritor);

        assertThat(dto).isEqualTo(new EscritorDTO("abc123", "1001", "Gabriel García Márquez", "Colombiana"));
    }

    @Test
    void debeRetornarNullCuandoLaEntradaEsNull() {
        assertThat(transformer.toEntity(null)).isNull();
        assertThat(transformer.toDTO(null)).isNull();
    }
}
