package com.epilogo.epilogo.exception;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Excepción lanzada cuando se intenta crear un usuario con un email ya registrado")
public class UserAlreadyExistsException extends RuntimeException {

    public UserAlreadyExistsException(String message) {
        super(message);
    }
}