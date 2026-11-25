package com.biblioteca.oriente.infraestructure.entry_points.reactive_web.escritor;

import com.biblioteca.oriente.domain.usecase.escritor.EscritorUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.net.URI;

@RestController
@RequiredArgsConstructor
@RequestMapping(EscritorController.ESCRITOR_PATH)
public class EscritorController {

    public static final String ESCRITOR_PATH = "/escritor";

    private final EscritorTransformer transformer;

    private final EscritorUseCase escritorUseCase;

    @PostMapping
    public Mono<ResponseEntity<EscritorDTO>> crear(@RequestBody EscritorDTO escritorDTO) {
        return escritorUseCase.crearEscritor(transformer.toEntity(escritorDTO))
                .map(transformer::toDTO)
                .map(creado -> ResponseEntity.created(ubicacion(creado.getIdentificacion())).body(creado));
    }

    @GetMapping(path = "/{identificacion}")
    public Mono<EscritorDTO> consultarPorIdentificacion(@PathVariable String identificacion) {
        return escritorUseCase.obtenerEscritorPorId(identificacion)
                .map(transformer::toDTO);
    }

    private static URI ubicacion(String identificacion) {
        return UriComponentsBuilder.fromPath(ESCRITOR_PATH)
                .pathSegment("{identificacion}")
                .buildAndExpand(identificacion)
                .encode()
                .toUri();
    }

}
