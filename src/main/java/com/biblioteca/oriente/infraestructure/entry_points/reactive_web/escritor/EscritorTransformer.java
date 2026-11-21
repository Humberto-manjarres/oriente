package com.biblioteca.oriente.infraestructure.entry_points.reactive_web.escritor;

import com.biblioteca.oriente.domain.model.escritor.Escritor;
import org.mapstruct.Mapper;

@Mapper
public interface EscritorTransformer {
    Escritor toEntity(EscritorDTO escritorDTO);
    EscritorDTO toDTO(Escritor escritor);
}
