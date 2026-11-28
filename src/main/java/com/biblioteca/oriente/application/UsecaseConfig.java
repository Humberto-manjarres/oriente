package com.biblioteca.oriente.application;

import com.biblioteca.oriente.domain.model.escritor.gateway.EscritorGateway;
import com.biblioteca.oriente.domain.model.libro.gatewy.LibroGateway;
import com.biblioteca.oriente.domain.usecase.escritor.EscritorUseCase;
import com.biblioteca.oriente.domain.usecase.libro.LibroUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UsecaseConfig {

    @Bean
    public EscritorUseCase escritorUseCase(EscritorGateway escritorGateway) {
        return new EscritorUseCase(escritorGateway);
    }

    @Bean
    public LibroUseCase libroUseCase(LibroGateway libroGateway) {
        return new LibroUseCase(libroGateway);
    }
}
