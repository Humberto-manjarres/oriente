package com.biblioteca.oriente.infraestructure.entry_points.reactive_web.exception;

import java.time.Instant;

public record ErrorResponse(
        int status,
        String error,
        String message,
        String path,
        Instant timestamp
) {}
