package com.epilogo.epilogo.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import io.swagger.v3.oas.annotations.media.Schema;

@ResponseStatus(HttpStatus.NOT_FOUND)
@Schema(description = "Excepción lanzada cuando un recurso solicitado no existe")
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}