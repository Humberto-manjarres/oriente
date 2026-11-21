package com.biblioteca.oriente.infraestructure.entry_points.reactive_web.escritor;

import com.biblioteca.oriente.domain.usecase.escritor.EscritorUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping("/escritor")
public class EscritorController {

    private final EscritorTransformer transformer;

    private final EscritorUseCase escritorUseCase;

    @PostMapping(path = "/crear")
    public Mono<EscritorDTO> crear(@RequestBody EscritorDTO escritorDTO){
        System.out.println("escritorDTO = " + escritorDTO);
        return escritorUseCase.crearEscritor(transformer.toEntity(escritorDTO))
                .map(transformer::toDTO);
    }


    @GetMapping(path = "/{escritorId}")
    public Mono<EscritorDTO> findByEscritorId(@PathVariable String escritorId) {
        return escritorUseCase.obtenerEscritorPorId(escritorId)
                .map(transformer::toDTO);
    }

}
