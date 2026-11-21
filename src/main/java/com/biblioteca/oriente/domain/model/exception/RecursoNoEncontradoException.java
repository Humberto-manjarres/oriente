package com.biblioteca.oriente.domain.model.exception;

public class RecursoNoEncontradoException extends RuntimeException{

    private final String recurso;
    private final String identificador;

    public RecursoNoEncontradoException(String recurso, String identificador) {
        super(recurso + " no encontrado con identificador: " + identificador);
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
