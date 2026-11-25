package com.biblioteca.oriente.infraestructure.entry_points.reactive_web.escritor;

import com.biblioteca.oriente.domain.model.escritor.Escritor;
import com.biblioteca.oriente.domain.model.exception.RecursoNoEncontradoException;
import com.biblioteca.oriente.domain.usecase.escritor.EscritorUseCase;
import com.biblioteca.oriente.infraestructure.entry_points.reactive_web.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EscritorControllerTest {

    @Mock
    private EscritorUseCase escritorUseCase;

    private WebTestClient webTestClient;

    @BeforeEach
    void setUp() {
        EscritorController controller =
                new EscritorController(Mappers.getMapper(EscritorTransformer.class), escritorUseCase);
        webTestClient = WebTestClient.bindToController(controller)
                .controllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void crearDebeRetornarEscritorCreado() {
        when(escritorUseCase.crearEscritor(any(Escritor.class)))
                .thenReturn(Mono.just(new Escritor("abc123", "1001", "Gabriel García Márquez", "Colombiana")));

        webTestClient.post()
                .uri("/escritor/crear")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new EscritorDTO(null, "1001", "Gabriel García Márquez", "Colombiana"))
                .exchange()
                .expectStatus().isOk()
                .expectBody(EscritorDTO.class)
                .isEqualTo(new EscritorDTO("abc123", "1001", "Gabriel García Márquez", "Colombiana"));
    }

    @Test
    void crearDebeRetornar500CuandoFallaElCasoDeUso() {
        when(escritorUseCase.crearEscritor(any(Escritor.class)))
                .thenReturn(Mono.error(new IllegalStateException("fallo mongo")));

        webTestClient.post()
                .uri("/escritor/crear")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new EscritorDTO(null, "1001", "Gabriel García Márquez", "Colombiana"))
                .exchange()
                .expectStatus().isEqualTo(500)
                .expectBody()
                .jsonPath("$.status").isEqualTo(500)
                .jsonPath("$.error").isEqualTo("Internal Server Error")
                .jsonPath("$.message").isEqualTo("Ocurrió un error interno en el servidor")
                .jsonPath("$.path").isEqualTo("/escritor/crear")
                .jsonPath("$.timestamp").exists();
    }

    @Test
    void findByEscritorIdDebeRetornarEscritorExistente() {
        when(escritorUseCase.obtenerEscritorPorId("1001"))
                .thenReturn(Mono.just(new Escritor("abc123", "1001", "Gabriel García Márquez", "Colombiana")));

        webTestClient.get()
                .uri("/escritor/{escritorId}", "1001")
                .exchange()
                .expectStatus().isOk()
                .expectBody(EscritorDTO.class)
                .isEqualTo(new EscritorDTO("abc123", "1001", "Gabriel García Márquez", "Colombiana"));
    }

    @Test
    void findByEscritorIdDebeRetornar404CuandoNoExiste() {
        when(escritorUseCase.obtenerEscritorPorId("9999"))
                .thenReturn(Mono.error(new RecursoNoEncontradoException("Escritor", "9999")));

        webTestClient.get()
                .uri("/escritor/{escritorId}", "9999")
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.status").isEqualTo(404)
                .jsonPath("$.error").isEqualTo("Not Found")
                .jsonPath("$.message").isEqualTo("Escritor no encontrado con identificador: 9999")
                .jsonPath("$.path").isEqualTo("/escritor/9999")
                .jsonPath("$.timestamp").exists();
    }
}
