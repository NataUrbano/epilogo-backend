package com.epilogo.epilogo.exception;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Hidden;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
@Tag(name = "Exception Handler", description = "Manejador global de excepciones de la API")
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ApiResponse(responseCode = "404", description = "Recurso no encontrado",
            content = @Content(schema = @Schema(implementation = com.epilogo.epilogo.exception.ErrorResponse.class)))
    public ResponseEntity<com.epilogo.epilogo.exception.ErrorResponse> handleResourceNotFoundException(ResourceNotFoundException ex) {
        com.epilogo.epilogo.exception.ErrorResponse errorResponse = new com.epilogo.epilogo.exception.ErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                "Recurso no encontrado",
                ex.getMessage()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    @ApiResponse(responseCode = "409", description = "Conflicto - El usuario ya existe",
            content = @Content(schema = @Schema(implementation = com.epilogo.epilogo.exception.ErrorResponse.class)))
    public ResponseEntity<com.epilogo.epilogo.exception.ErrorResponse> handleUserAlreadyExistsException(UserAlreadyExistsException ex) {
        com.epilogo.epilogo.exception.ErrorResponse errorResponse = new com.epilogo.epilogo.exception.ErrorResponse(
                HttpStatus.CONFLICT.value(),
                "Conflicto",
                ex.getMessage()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(S3FileException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ApiResponse(responseCode = "500", description = "Error de almacenamiento en S3",
            content = @Content(schema = @Schema(implementation = com.epilogo.epilogo.exception.ErrorResponse.class)))
    public ResponseEntity<com.epilogo.epilogo.exception.ErrorResponse> handleS3FileException(S3FileException ex) {
        log.error("Error de Amazon S3: {}", ex.getMessage());
        com.epilogo.epilogo.exception.ErrorResponse errorResponse = new com.epilogo.epilogo.exception.ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Error de almacenamiento",
                "Error al procesar el archivo. Por favor, inténtelo de nuevo."
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(IllegalStateException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ApiResponse(responseCode = "400", description = "Solicitud inválida",
            content = @Content(schema = @Schema(implementation = com.epilogo.epilogo.exception.ErrorResponse.class)))
    public ResponseEntity<com.epilogo.epilogo.exception.ErrorResponse> handleIllegalStateException(IllegalStateException ex) {
        com.epilogo.epilogo.exception.ErrorResponse errorResponse = new com.epilogo.epilogo.exception.ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Solicitud inválida",
                ex.getMessage()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ApiResponse(responseCode = "400", description = "Error de validación en los datos proporcionados",
            content = @Content(schema = @Schema(implementation = ValidationErrorResponse.class)))
    public ResponseEntity<ValidationErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        ValidationErrorResponse errorResponse = new ValidationErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Error de validación",
                "Hay errores en los datos proporcionados",
                errors
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ApiResponse(responseCode = "403", description = "Acceso denegado",
            content = @Content(schema = @Schema(implementation = com.epilogo.epilogo.exception.ErrorResponse.class)))
    public ResponseEntity<com.epilogo.epilogo.exception.ErrorResponse> handleAccessDeniedException(AccessDeniedException ex) {
        com.epilogo.epilogo.exception.ErrorResponse errorResponse = new com.epilogo.epilogo.exception.ErrorResponse(
                HttpStatus.FORBIDDEN.value(),
                "Acceso denegado",
                "No tiene permisos para realizar esta acción"
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(BadCredentialsException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ApiResponse(responseCode = "401", description = "Credenciales inválidas",
            content = @Content(schema = @Schema(implementation = com.epilogo.epilogo.exception.ErrorResponse.class)))
    public ResponseEntity<com.epilogo.epilogo.exception.ErrorResponse> handleBadCredentialsException(BadCredentialsException ex) {
        com.epilogo.epilogo.exception.ErrorResponse errorResponse = new com.epilogo.epilogo.exception.ErrorResponse(
                HttpStatus.UNAUTHORIZED.value(),
                "Credenciales inválidas",
                "Las credenciales proporcionadas son incorrectas"
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ApiResponse(responseCode = "401", description = "Usuario no encontrado",
            content = @Content(schema = @Schema(implementation = com.epilogo.epilogo.exception.ErrorResponse.class)))
    public ResponseEntity<com.epilogo.epilogo.exception.ErrorResponse> handleUsernameNotFoundException(UsernameNotFoundException ex) {
        com.epilogo.epilogo.exception.ErrorResponse errorResponse = new com.epilogo.epilogo.exception.ErrorResponse(
                HttpStatus.UNAUTHORIZED.value(),
                "Usuario no encontrado",
                ex.getMessage()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler({ExpiredJwtException.class, MalformedJwtException.class, UnsupportedJwtException.class, SignatureException.class})
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ApiResponse(responseCode = "401", description = "Error de autenticación JWT",
            content = @Content(schema = @Schema(implementation = com.epilogo.epilogo.exception.ErrorResponse.class)))
    public ResponseEntity<com.epilogo.epilogo.exception.ErrorResponse> handleJwtExceptions(Exception ex) {
        String reason = "Error de autenticación";
        if (ex instanceof ExpiredJwtException) {
            reason = "Su sesión ha expirado. Por favor, inicie sesión nuevamente.";
        } else if (ex instanceof MalformedJwtException || ex instanceof SignatureException) {
            reason = "Token de autenticación inválido";
        }

        com.epilogo.epilogo.exception.ErrorResponse errorResponse = new com.epilogo.epilogo.exception.ErrorResponse(
                HttpStatus.UNAUTHORIZED.value(),
                "Error de autenticación",
                reason
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    @ResponseStatus(HttpStatus.PAYLOAD_TOO_LARGE)
    @ApiResponse(responseCode = "413", description = "Archivo demasiado grande",
            content = @Content(schema = @Schema(implementation = com.epilogo.epilogo.exception.ErrorResponse.class)))
    public ResponseEntity<com.epilogo.epilogo.exception.ErrorResponse> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException ex) {
        com.epilogo.epilogo.exception.ErrorResponse errorResponse = new com.epilogo.epilogo.exception.ErrorResponse(
                HttpStatus.PAYLOAD_TOO_LARGE.value(),
                "Archivo demasiado grande",
                "El tamaño del archivo excede el límite permitido (10MB)"
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.PAYLOAD_TOO_LARGE);
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ApiResponse(responseCode = "500", description = "Error interno del servidor",
            content = @Content(schema = @Schema(implementation = com.epilogo.epilogo.exception.ErrorResponse.class)))
    public ResponseEntity<com.epilogo.epilogo.exception.ErrorResponse> handleGenericException(Exception ex) {
        log.error("Error no controlado: ", ex);
        com.epilogo.epilogo.exception.ErrorResponse errorResponse = new com.epilogo.epilogo.exception.ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Error interno del servidor",
                "Ha ocurrido un error inesperado. Por favor, contacte al administrador."
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}