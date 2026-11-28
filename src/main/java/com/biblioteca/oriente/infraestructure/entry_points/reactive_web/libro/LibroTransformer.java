package com.biblioteca.oriente.infraestructure.entry_points.reactive_web.libro;

import com.biblioteca.oriente.domain.model.libro.Libro;
import org.mapstruct.Mapper;

@Mapper
public interface LibroTransformer {
    LibroDTO toDTO(Libro libro);
    Libro toEntity(LibroDTO libroDTO);
}
