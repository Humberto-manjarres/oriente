package com.biblioteca.oriente.domain.usecase.escritor;

import com.biblioteca.oriente.domain.model.escritor.Escritor;
import com.biblioteca.oriente.domain.model.escritor.gateway.EscritorGateway;
import com.biblioteca.oriente.domain.model.exception.CampoObligatorioException;
import com.biblioteca.oriente.domain.model.exception.RecursoDuplicadoException;
import com.biblioteca.oriente.domain.model.exception.RecursoNoEncontradoException;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class EscritorUseCase {

    private static final String RECURSO = "Escritor";

    private final EscritorGateway escritorGateway;

    /**
     * Crea el escritor si trae los campos obligatorios (identificacion y nombre)
     * y no existe otro con la misma identificación.
     */
    public Mono<Escritor> crearEscritor(Escritor escritor) {
        List<String> faltantes = camposObligatoriosFaltantes(escritor);
        if (!faltantes.isEmpty()) {
            return Mono.error(new CampoObligatorioException(RECURSO, faltantes));
        }
        return escritorGateway.consultarEscritor(escritor.getIdentificacion())
                .flatMap(existente -> Mono.<Escritor>error(
                        new RecursoDuplicadoException(RECURSO, escritor.getIdentificacion())))
                .switchIfEmpty(Mono.defer(() -> escritorGateway.crearEscritor(escritor)));
    }

    public Mono<Escritor> obtenerEscritorPorId(String identificacion) {
        return escritorGateway.consultarEscritor(identificacion)
                .switchIfEmpty(Mono.error(() -> new RecursoNoEncontradoException(RECURSO, identificacion)));
    }

    private static List<String> camposObligatoriosFaltantes(Escritor escritor) {
        List<String> faltantes = new ArrayList<>();
        if (estaVacio(escritor.getIdentificacion())) {
            faltantes.add("identificacion");
        }
        if (estaVacio(escritor.getNombre())) {
            faltantes.add("nombre");
        }
        return faltantes;
    }

    private static boolean estaVacio(String valor) {
        return valor == null || valor.isBlank();
    }

}
