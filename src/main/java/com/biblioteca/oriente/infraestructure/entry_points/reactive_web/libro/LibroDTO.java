package com.biblioteca.oriente.infraestructure.entry_points.reactive_web.libro;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LibroDTO {
    private String id;
    private String descripcion;
}
