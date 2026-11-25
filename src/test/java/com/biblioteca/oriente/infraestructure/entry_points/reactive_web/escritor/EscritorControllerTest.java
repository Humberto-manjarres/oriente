package com.biblioteca.oriente.infraestructure.entry_points.reactive_web.escritor;

import com.biblioteca.oriente.domain.model.escritor.Escritor;
import com.biblioteca.oriente.domain.model.exception.CampoObligatorioException;
import com.biblioteca.oriente.domain.model.exception.RecursoDuplicadoException;
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

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EscritorControllerTest {

    private static final String MENSAJE_SOLICITUD_INVALIDA =
            "La solicitud es inválida: el cuerpo está vacío, no es un JSON válido o tiene parámetros incorrectos";

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

    private static EscritorDTO solicitud() {
        return new EscritorDTO(null, "1001", "Gabriel García Márquez", "Colombiana");
    }

    @Test
    void crearDebeRetornar201ConLocationYEscritorCreado() {
        when(escritorUseCase.crearEscritor(any(Escritor.class)))
                .thenReturn(Mono.just(new Escritor("abc123", "1001", "Gabriel García Márquez", "Colombiana")));

        webTestClient.post()
                .uri("/escritor")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(solicitud())
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().location("/escritor/1001")
                .expectBody(EscritorDTO.class)
                .isEqualTo(new EscritorDTO("abc123", "1001", "Gabriel García Márquez", "Colombiana"));
    }

    @Test
    void crearDebeCodificarLaIdentificacionEnLocation() {
        when(escritorUseCase.crearEscritor(any(Escritor.class)))
                .thenReturn(Mono.just(new Escritor("abc123", "CC 10/01", "Gabriel García Márquez", "Colombiana")));

        webTestClient.post()
                .uri("/escritor")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(solicitud())
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().location("/escritor/CC%2010%2F01");
    }

    @Test
    void crearDebeRetornar409CuandoLaIdentificacionYaExiste() {
        when(escritorUseCase.crearEscritor(any(Escritor.class)))
                .thenReturn(Mono.error(new RecursoDuplicadoException("Escritor", "1001")));

        webTestClient.post()
                .uri("/escritor")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(solicitud())
                .exchange()
                .expectStatus().isEqualTo(409)
                .expectBody()
                .jsonPath("$.status").isEqualTo(409)
                .jsonPath("$.error").isEqualTo("Conflict")
                .jsonPath("$.message").isEqualTo("Escritor ya existe con identificador: 1001")
                .jsonPath("$.path").isEqualTo("/escritor")
                .jsonPath("$.timestamp").exists();
    }

    @Test
    void crearDebeRetornar400CuandoFaltanCamposObligatorios() {
        when(escritorUseCase.crearEscritor(any(Escritor.class)))
                .thenReturn(Mono.error(new CampoObligatorioException("Escritor", List.of("identificacion", "nombre"))));

        webTestClient.post()
                .uri("/escritor")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"nacionalidad\": \"Colombiana\"}")
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.status").isEqualTo(400)
                .jsonPath("$.error").isEqualTo("Bad Request")
                .jsonPath("$.message").isEqualTo("Escritor: los siguientes campos son obligatorios: identificacion, nombre")
                .jsonPath("$.path").isEqualTo("/escritor");
    }

    @Test
    void crearDebeRetornar500CuandoFallaElCasoDeUso() {
        when(escritorUseCase.crearEscritor(any(Escritor.class)))
                .thenReturn(Mono.error(new IllegalStateException("fallo mongo")));

        webTestClient.post()
                .uri("/escritor")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(solicitud())
                .exchange()
                .expectStatus().isEqualTo(500)
                .expectBody()
                .jsonPath("$.status").isEqualTo(500)
                .jsonPath("$.error").isEqualTo("Internal Server Error")
                .jsonPath("$.message").isEqualTo("Ocurrió un error interno en el servidor")
                .jsonPath("$.path").isEqualTo("/escritor");
    }

    @Test
    void crearDebeRetornar400CuandoElJsonEsInvalido() {
        webTestClient.post()
                .uri("/escritor")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"identificacion\": \"1001\", \"nombre\": ")
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.status").isEqualTo(400)
                .jsonPath("$.error").isEqualTo("Bad Request")
                .jsonPath("$.message").isEqualTo(MENSAJE_SOLICITUD_INVALIDA)
                .jsonPath("$.path").isEqualTo("/escritor");

        verifyNoInteractions(escritorUseCase);
    }

    @Test
    void crearDebeRetornar400CuandoNoHayCuerpo() {
        webTestClient.post()
                .uri("/escritor")
                .contentType(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.status").isEqualTo(400);

        verifyNoInteractions(escritorUseCase);
    }

    @Test
    void crearDebeRetornar415CuandoElContentTypeNoEsJson() {
        webTestClient.post()
                .uri("/escritor")
                .contentType(MediaType.TEXT_PLAIN)
                .bodyValue("identificacion=1001")
                .exchange()
                .expectStatus().isEqualTo(415)
                .expectBody()
                .jsonPath("$.status").isEqualTo(415)
                .jsonPath("$.error").isEqualTo("Unsupported Media Type");

        verifyNoInteractions(escritorUseCase);
    }

    @Test
    void debeRetornar405CuandoElMetodoNoEstaPermitido() {
        webTestClient.put()
                .uri("/escritor")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{}")
                .exchange()
                .expectStatus().isEqualTo(405)
                .expectBody()
                .jsonPath("$.status").isEqualTo(405)
                .jsonPath("$.error").isEqualTo("Method Not Allowed");

        verifyNoInteractions(escritorUseCase);
    }

    @Test
    void consultarPorIdentificacionDebeRetornarEscritorExistente() {
        when(escritorUseCase.obtenerEscritorPorId("1001"))
                .thenReturn(Mono.just(new Escritor("abc123", "1001", "Gabriel García Márquez", "Colombiana")));

        webTestClient.get()
                .uri("/escritor/{identificacion}", "1001")
                .exchange()
                .expectStatus().isOk()
                .expectBody(EscritorDTO.class)
                .isEqualTo(new EscritorDTO("abc123", "1001", "Gabriel García Márquez", "Colombiana"));
    }

    @Test
    void consultarPorIdentificacionDebeRetornar404CuandoNoExiste() {
        when(escritorUseCase.obtenerEscritorPorId("9999"))
                .thenReturn(Mono.error(new RecursoNoEncontradoException("Escritor", "9999")));

        webTestClient.get()
                .uri("/escritor/{identificacion}", "9999")
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.status").isEqualTo(404)
                .jsonPath("$.error").isEqualTo("Not Found")
                .jsonPath("$.message").isEqualTo("Escritor no encontrado con identificador: 9999")
                .jsonPath("$.path").isEqualTo("/escritor/9999")
                .jsonPath("$.timestamp").exists();
    }

    @Test
    void laRutaCrearAnteriorYaNoExisteParaPost() {
        webTestClient.post()
                .uri("/escritor/crear")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(solicitud())
                .exchange()
                .expectStatus().isEqualTo(405);

        verifyNoInteractions(escritorUseCase);
    }
}
