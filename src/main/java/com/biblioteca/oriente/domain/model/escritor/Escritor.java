package com.biblioteca.oriente.domain.model.escritor;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Escritor {
    private String id;
    private String identificacion; // Identificador único del escritor
    private String nombre;        // Nombre del escritor
    private String nacionalidad;  // Nacionalidad del escritor
}
