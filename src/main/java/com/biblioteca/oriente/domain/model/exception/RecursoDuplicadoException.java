package com.biblioteca.oriente.domain.model.exception;

public class RecursoDuplicadoException extends RuntimeException {

    private final String recurso;
    private final String identificador;

    public RecursoDuplicadoException(String recurso, String identificador) {
        super(recurso + " ya existe con identificador: " + identificador);
        this.recurso = recurso;
        this.identificador = identificador;
    }

    public String getRecurso() {
        return recurso;
    }

    public String getIdentificador() {
        return identificador;
    }

}
