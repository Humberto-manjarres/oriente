package com.biblioteca.oriente.application;

import com.biblioteca.oriente.infraestructure.driven_adapters.reactive_repository.escritor_mongo.EscritorTransformerRepository;
import com.biblioteca.oriente.infraestructure.driven_adapters.reactive_repository.libro_mongo.LibroTransformerRepository;
import com.biblioteca.oriente.infraestructure.entry_points.reactive_web.escritor.EscritorTransformer;
import com.biblioteca.oriente.infraestructure.entry_points.reactive_web.libro.LibroTransformer;
import org.mapstruct.factory.Mappers;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TransformerConfig {

    @Bean
    public EscritorTransformer escritorTransformer() {
        return Mappers.getMapper(EscritorTransformer.class);
    }

    @Bean
    public EscritorTransformerRepository escritorTransformerRepository() {
        return Mappers.getMapper(EscritorTransformerRepository.class);
    }

    @Bean
    public LibroTransformer libroTransformer() {
        return Mappers.getMapper(LibroTransformer.class);
    }

    @Bean
    public LibroTransformerRepository libroTransformerRepository() {
        return Mappers.getMapper(LibroTransformerRepository.class);
    }

}
