package com.biblioteca.oriente.domain.model.exception;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RecursoDuplicadoExceptionTest {

    @Test
    void debeConstruirMensajeConRecursoEIdentificador() {
        RecursoDuplicadoException exception = new RecursoDuplicadoException("Escritor", "1001");

        assertThat(exception)
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Escritor ya existe con identificador: 1001");
        assertThat(exception.getRecurso()).isEqualTo("Escritor");
        assertThat(exception.getIdentificador()).isEqualTo("1001");
    }
}
