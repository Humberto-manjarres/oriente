package com.biblioteca.oriente.infraestructure.entry_points.reactive_web.escritor;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EscritorDTO {
    private String id;
    private String identificacion;
    private String nombre;
    private String nacionalidad;
}
