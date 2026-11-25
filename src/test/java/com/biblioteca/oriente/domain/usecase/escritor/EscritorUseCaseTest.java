package com.biblioteca.oriente.domain.usecase.escritor;

import com.biblioteca.oriente.domain.model.escritor.Escritor;
import com.biblioteca.oriente.domain.model.escritor.gateway.EscritorGateway;
import com.biblioteca.oriente.domain.model.exception.CampoObligatorioException;
import com.biblioteca.oriente.domain.model.exception.RecursoDuplicadoException;
import com.biblioteca.oriente.domain.model.exception.RecursoNoEncontradoException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
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
    void crearEscritorDebeGuardarCuandoLaIdentificacionNoExiste() {
        Escritor nuevo = escritorNuevo();
        Escritor guardado = escritorGuardado();
        when(escritorGateway.consultarEscritor("1001")).thenReturn(Mono.empty());
        when(escritorGateway.crearEscritor(nuevo)).thenReturn(Mono.just(guardado));

        StepVerifier.create(escritorUseCase.crearEscritor(nuevo))
                .expectNext(guardado)
                .verifyComplete();

        verify(escritorGateway).crearEscritor(nuevo);
    }

    @Test
    void crearEscritorDebePermitirNacionalidadVacia() {
        Escritor sinNacionalidad = Escritor.builder().identificacion("1001").nombre("Gabriel García Márquez").build();
        Escritor guardado = new Escritor("abc123", "1001", "Gabriel García Márquez", null);
        when(escritorGateway.consultarEscritor("1001")).thenReturn(Mono.empty());
        when(escritorGateway.crearEscritor(sinNacionalidad)).thenReturn(Mono.just(guardado));

        StepVerifier.create(escritorUseCase.crearEscritor(sinNacionalidad))
                .expectNext(guardado)
                .verifyComplete();
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t"})
    void crearEscritorDebeFallarCuandoFaltaIdentificacion(String identificacion) {
        Escritor escritor = Escritor.builder().identificacion(identificacion).nombre("Gabriel García Márquez").build();

        StepVerifier.create(escritorUseCase.crearEscritor(escritor))
                .expectErrorSatisfies(error -> {
                    assertThat(error)
                            .isInstanceOf(CampoObligatorioException.class)
                            .hasMessage("Escritor: los siguientes campos son obligatorios: identificacion");
                    assertThat(((CampoObligatorioException) error).getCampos()).containsExactly("identificacion");
                })
                .verify();

        verifyNoInteractions(escritorGateway);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t"})
    void crearEscritorDebeFallarCuandoFaltaNombre(String nombre) {
        Escritor escritor = Escritor.builder().identificacion("1001").nombre(nombre).build();

        StepVerifier.create(escritorUseCase.crearEscritor(escritor))
                .expectErrorSatisfies(error -> {
                    assertThat(error)
                            .isInstanceOf(CampoObligatorioException.class)
                            .hasMessage("Escritor: los siguientes campos son obligatorios: nombre");
                    assertThat(((CampoObligatorioException) error).getCampos()).containsExactly("nombre");
                })
                .verify();

        verifyNoInteractions(escritorGateway);
    }

    @Test
    void crearEscritorDebeReportarTodosLosCamposFaltantes() {
        Escritor escritor = Escritor.builder().nacionalidad("Colombiana").build();

        StepVerifier.create(escritorUseCase.crearEscritor(escritor))
                .expectErrorSatisfies(error -> {
                    assertThat(error)
                            .isInstanceOf(CampoObligatorioException.class)
                            .hasMessage("Escritor: los siguientes campos son obligatorios: identificacion, nombre");
                    CampoObligatorioException faltantes = (CampoObligatorioException) error;
                    assertThat(faltantes.getRecurso()).isEqualTo("Escritor");
                    assertThat(faltantes.getCampos()).containsExactly("identificacion", "nombre");
                })
                .verify();

        verifyNoInteractions(escritorGateway);
    }

    @Test
    void crearEscritorDebeFallarConDuplicadoCuandoLaIdentificacionYaExiste() {
        when(escritorGateway.consultarEscritor("1001")).thenReturn(Mono.just(escritorGuardado()));

        StepVerifier.create(escritorUseCase.crearEscritor(escritorNuevo()))
                .expectErrorSatisfies(error -> {
                    assertThat(error)
                            .isInstanceOf(RecursoDuplicadoException.class)
                            .hasMessage("Escritor ya existe con identificador: 1001");
                    RecursoDuplicadoException duplicado = (RecursoDuplicadoException) error;
                    assertThat(duplicado.getRecurso()).isEqualTo("Escritor");
                    assertThat(duplicado.getIdentificador()).isEqualTo("1001");
                })
                .verify();

        verify(escritorGateway, never()).crearEscritor(any());
    }

    @Test
    void crearEscritorDebePropagarErrorAlGuardar() {
        Escritor nuevo = escritorNuevo();
        when(escritorGateway.consultarEscritor("1001")).thenReturn(Mono.empty());
        when(escritorGateway.crearEscritor(nuevo)).thenReturn(Mono.error(new IllegalStateException("fallo mongo")));

        StepVerifier.create(escritorUseCase.crearEscritor(nuevo))
                .expectErrorMessage("fallo mongo")
                .verify();
    }

    @Test
    void crearEscritorDebePropagarErrorAlConsultarSinGuardar() {
        when(escritorGateway.consultarEscritor("1001")).thenReturn(Mono.error(new IllegalStateException("fallo mongo")));

        StepVerifier.create(escritorUseCase.crearEscritor(escritorNuevo()))
                .expectErrorMessage("fallo mongo")
                .verify();

        verify(escritorGateway, never()).crearEscritor(any());
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
