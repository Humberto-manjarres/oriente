package com.biblioteca.oriente.infraestructure.driven_adapters.reactive_repository.libro_mongo;

import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
@Document(collection = "libros")
public class LibroEntity {
    private String id;
    private String codigoLibro;
    private String nombre;
    private Integer valorUnitario;
    private String escritorId;
    private String categoriaId;
}
