package com.epilogo.epilogo.exception;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Excepción lanzada cuando ocurre un error relacionado con el servicio S3")
public class S3FileException extends RuntimeException {

    public S3FileException(String message) {
        super(message);
    }

    public S3FileException(String message, Throwable cause) {
        super(message, cause);
    }
}