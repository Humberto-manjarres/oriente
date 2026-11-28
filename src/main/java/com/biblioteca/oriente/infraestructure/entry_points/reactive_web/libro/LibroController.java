package com.biblioteca.oriente.infraestructure.entry_points.reactive_web.libro;

import com.biblioteca.oriente.domain.usecase.libro.LibroUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.net.URI;

@RestController
@RequiredArgsConstructor
@RequestMapping(LibroController.LIBRO_PATH)
public class LibroController {

    public static final String LIBRO_PATH = "/libro";

    private final LibroTransformer transformer;

    private final LibroUseCase libroUseCase;

    @PostMapping
    public Mono<ResponseEntity<LibroDTO>> crear(@RequestBody LibroDTO libroDTO) {
        return libroUseCase.crearLibro(transformer.toEntity(libroDTO))
                .map(transformer::toDTO)
                .map(creado -> ResponseEntity.created(ubicacion(creado.getId())).body(creado));
    }

    @GetMapping(path = "/{idLibro}")
    public Mono<LibroDTO> consultarPorIsbn(@PathVariable String idLibro) {
        return libroUseCase.consultarLibro(idLibro)
                .map(transformer::toDTO);
    }

    private static URI ubicacion(String isbn) {
        return UriComponentsBuilder.fromPath(LIBRO_PATH)
                .pathSegment("{idLibro}")
                .buildAndExpand(isbn)
                .encode()
                .toUri();
    }

}
