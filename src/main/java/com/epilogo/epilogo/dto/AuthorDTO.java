package com.epilogo.epilogo.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "DTOs relacionados con la gestión de autores")
public class AuthorDTO {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(name = "AuthorResponse", description = "Respuesta con los datos completos de un autor")
    public static class AuthorResponse {
        @Schema(description = "Identificador único del autor", example = "1")
        private Long authorId;

        @Schema(description = "Nombre completo del autor", example = "Gabriel García Márquez")
        private String authorName;

        @Schema(description = "Biografía del autor", example = "Gabriel García Márquez fue un escritor colombiano ganador del Premio Nobel de Literatura en 1982...")
        private String biography;

        @Schema(description = "Año de nacimiento del autor", example = "1927")
        private Integer birthYear;

        @Schema(description = "Año de fallecimiento del autor (si aplica)", example = "2014")
        private Integer deathYear;

        @Schema(description = "URL de la imagen del autor", example = "https://epilogo-bucket.s3.amazonaws.com/authors/gabriel-garcia-marquez.jpg")
        private String imageUrl;

        @Schema(description = "Lista de libros escritos por este autor")
        private List<BookDTO.BookSummary> books;

        @Schema(description = "Número total de libros de este autor", example = "15")
        private Integer totalBooks;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(name = "AuthorSummary", description = "Resumen básico de un autor")
    public static class AuthorSummary {
        @Schema(description = "Identificador único del autor", example = "1")
        private Long authorId;

        @Schema(description = "Nombre completo del autor", example = "Gabriel García Márquez")
        private String authorName;

        @Schema(description = "URL de la imagen del autor", example = "https://epilogo-bucket.s3.amazonaws.com/authors/gabriel-garcia-marquez.jpg")
        private String imageUrl;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(name = "AuthorCreateRequest", description = "Datos requeridos para crear un nuevo autor")
    public static class AuthorCreateRequest {
        @NotBlank(message = "El nombre del autor es obligatorio")
        @Schema(description = "Nombre completo del autor", example = "Gabriel García Márquez", required = true)
        private String authorName;

        @Schema(description = "Biografía del autor", example = "Gabriel García Márquez fue un escritor colombiano ganador del Premio Nobel de Literatura...")
        private String biography;

        @Schema(description = "Año de nacimiento del autor", example = "1927")
        private Integer birthYear;

        @Schema(description = "Año de fallecimiento del autor (si aplica)", example = "2014")
        private Integer deathYear;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(name = "AuthorUpdateRequest", description = "Datos para actualizar un autor existente")
    public static class AuthorUpdateRequest {
        @Schema(description = "Nuevo nombre del autor", example = "Gabriel José García Márquez")
        private String authorName;

        @Schema(description = "Nueva biografía del autor", example = "Actualización de la biografía del autor...")
        private String biography;

        @Schema(description = "Nuevo año de nacimiento", example = "1927")
        private Integer birthYear;

        @Schema(description = "Nuevo año de fallecimiento", example = "2014")
        private Integer deathYear;
    }
}