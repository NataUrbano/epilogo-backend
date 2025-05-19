package com.epilogo.epilogo.exception;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Estructura estándar para respuestas de error")
public class ErrorResponse {
    @Schema(description = "Código de estado HTTP", example = "404")
    private int status;

    @Schema(description = "Tipo de error", example = "Recurso no encontrado")
    private String error;

    @Schema(description = "Mensaje descriptivo del error", example = "El libro con ID 123 no fue encontrado")
    private String message;

    @Schema(description = "Fecha y hora en que ocurrió el error", example = "2023-05-18T15:30:45")
    private LocalDateTime timestamp = LocalDateTime.now();

    public ErrorResponse(int status, String error, String message) {
        this.status = status;
        this.error = error;
        this.message = message;
    }
}