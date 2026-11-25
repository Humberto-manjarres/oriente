package com.biblioteca.oriente.domain.model.exception;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CampoObligatorioExceptionTest {

    @Test
    void debeConstruirMensajeConTodosLosCampos() {
        CampoObligatorioException exception =
                new CampoObligatorioException("Escritor", List.of("identificacion", "nombre"));

        assertThat(exception)
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Escritor: los siguientes campos son obligatorios: identificacion, nombre");
        assertThat(exception.getRecurso()).isEqualTo("Escritor");
        assertThat(exception.getCampos()).containsExactly("identificacion", "nombre");
    }

    @Test
    void debeGuardarUnaCopiaInmutableDeLosCampos() {
        List<String> campos = new ArrayList<>(List.of("nombre"));

        CampoObligatorioException exception = new CampoObligatorioException("Escritor", campos);
        campos.add("identificacion");

        assertThat(exception.getCampos()).containsExactly("nombre");
    }
}
