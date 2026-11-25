package com.biblioteca.oriente.domain.usecase.escritor;

import com.biblioteca.oriente.domain.model.escritor.Escritor;
import com.biblioteca.oriente.domain.model.escritor.gateway.EscritorGateway;
import com.biblioteca.oriente.domain.model.exception.RecursoNoEncontradoException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EscritorUseCaseTest {

    @Mock
    private EscritorGateway escritorGateway;

    @InjectMocks
    private EscritorUseCase escritorUseCase;

    private static Escritor escritorNuevo() {
        return Escritor.builder()
                .identificacion("1001")
                .nombre("Gabriel García Márquez")
                .nacionalidad("Colombiana")
                .build();
    }

    private static Escritor escritorGuardado() {
        Escritor guardado = escritorNuevo();
        guardado.setId("abc123");
        return guardado;
    }

    @Test
    void crearEscritorDebeRetornarEscritorGuardado() {
        Escritor nuevo = escritorNuevo();
        Escritor guardado = escritorGuardado();
        when(escritorGateway.crearEscritor(nuevo)).thenReturn(Mono.just(guardado));

        StepVerifier.create(escritorUseCase.crearEscritor(nuevo))
                .expectNext(guardado)
                .verifyComplete();

        verify(escritorGateway).crearEscritor(nuevo);
    }

    @Test
    void crearEscritorDebePropagarErrorDelGateway() {
        Escritor nuevo = escritorNuevo();
        when(escritorGateway.crearEscritor(nuevo)).thenReturn(Mono.error(new IllegalStateException("fallo mongo")));

        StepVerifier.create(escritorUseCase.crearEscritor(nuevo))
                .expectErrorMessage("fallo mongo")
                .verify();
    }

    @Test
    void obtenerEscritorPorIdDebeRetornarEscritorExistente() {
        Escritor guardado = escritorGuardado();
        when(escritorGateway.consultarEscritor("1001")).thenReturn(Mono.just(guardado));

        StepVerifier.create(escritorUseCase.obtenerEscritorPorId("1001"))
                .expectNext(guardado)
                .verifyComplete();
    }

    @Test
    void obtenerEscritorPorIdDebeFallarConRecursoNoEncontradoCuandoNoExiste() {
        when(escritorGateway.consultarEscritor("9999")).thenReturn(Mono.empty());

        StepVerifier.create(escritorUseCase.obtenerEscritorPorId("9999"))
                .expectErrorSatisfies(error -> {
                    assertThat(error).isInstanceOf(RecursoNoEncontradoException.class);
                    RecursoNoEncontradoException noEncontrado = (RecursoNoEncontradoException) error;
                    assertThat(noEncontrado.getRecurso()).isEqualTo("Escritor");
                    assertThat(noEncontrado.getIdentificador()).isEqualTo("9999");
                })
                .verify();
    }

    @Test
    void obtenerEscritorPorIdDebePropagarErrorDelGateway() {
        when(escritorGateway.consultarEscritor("1001")).thenReturn(Mono.error(new IllegalStateException("fallo mongo")));

        StepVerifier.create(escritorUseCase.obtenerEscritorPorId("1001"))
                .expectErrorMessage("fallo mongo")
                .verify();
    }
}
