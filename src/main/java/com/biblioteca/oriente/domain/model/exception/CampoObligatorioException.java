package com.biblioteca.oriente.domain.model.exception;

import java.util.List;

public class CampoObligatorioException extends RuntimeException {

    private final String recurso;
    private final List<String> campos;

    public CampoObligatorioException(String recurso, List<String> campos) {
        super(recurso + ": los siguientes campos son obligatorios: " + String.join(", ", campos));
        this.recurso = recurso;
        this.campos = List.copyOf(campos);
    }

    public String getRecurso() {
        return recurso;
    }

    public List<String> getCampos() {
        return campos;
    }

}
