package com.biblioteca.oriente.infraestructure.driven_adapters.reactive_web.libro;

import com.biblioteca.oriente.domain.model.libro.Caratula;
import com.biblioteca.oriente.domain.model.libro.gatewy.LibroConsumerGateway;
import com.biblioteca.oriente.infraestructure.driven_adapters.reactive_web.base.Consumer;
import lombok.Getter;
import lombok.Setter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;


@Service
@Getter
public class LibroConsumerAdapter extends Consumer implements LibroConsumerGateway {

    private final Logger logger = LogManager.getLogger(this.getClass());

    @Setter
    @Value("${caratula.uri}")
    private String uri;

    protected LibroConsumerAdapter(@Value("${api.caratula.url}") String baseUrl) {
        super(baseUrl);
    }

    @Override
    public Mono<Caratula> consultarCaratulaLibro(String idLibro) {
        return getRequest(uri.concat(idLibro.toString()), Caratula.class).onErrorResume (
                throwable -> {
                    logger.error(String.format("Error al consultar caratula del libro", idLibro), throwable);
                    return Mono.just(Caratula.builder().build());
                }
        );
    }

    @Override
    public void configureHeaders(HttpHeaders headers) {

    }
}
