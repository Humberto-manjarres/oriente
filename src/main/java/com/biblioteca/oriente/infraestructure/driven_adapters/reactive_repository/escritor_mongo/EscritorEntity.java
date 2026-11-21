package com.biblioteca.oriente.infraestructure.driven_adapters.reactive_repository.escritor_mongo;


import lombok.*;

import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
@Document(collection = "escritores")
public class EscritorEntity {

    private String id;
    private String identificacion;
    private String nombre;
    private String nacionalidad;
}
