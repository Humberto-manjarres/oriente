package com.biblioteca.oriente.domain.model.exception;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RecursoNoEncontradoExceptionTest {

    @Test
    void debeConstruirMensajeConRecursoEIdentificador() {
        RecursoNoEncontradoException exception = new RecursoNoEncontradoException("Escritor", "1001");

        assertThat(exception)
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Escritor no encontrado con identificador: 1001");
        assertThat(exception.getRecurso()).isEqualTo("Escritor");
        assertThat(exception.getIdentificador()).isEqualTo("1001");
    }
}
