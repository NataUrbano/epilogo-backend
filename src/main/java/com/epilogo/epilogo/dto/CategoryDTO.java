package com.epilogo.epilogo.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "DTOs relacionados con la gestión de categorías de libros")
public class CategoryDTO {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(name = "CategoryResponse", description = "Respuesta con los datos completos de una categoría")
    public static class CategoryResponse {
        @Schema(description = "Identificador único de la categoría", example = "1")
        private Long categoryId;

        @Schema(description = "Nombre de la categoría", example = "Ciencia Ficción")
        private String categoryName;

        @Schema(description = "Descripción detallada de la categoría", example = "Libros que exploran futuros alternativos y tecnología avanzada")
        private String description;

        @Schema(description = "URL de la imagen que representa la categoría", example = "https://epilogo-bucket.s3.amazonaws.com/categories/sci-fi.jpg")
        private String imageUrl;

        @Schema(description = "Lista de libros que pertenecen a esta categoría")
        private List<BookDTO.BookSummary> books;

        @Schema(description = "Número total de libros en esta categoría", example = "42")
        private Integer totalBooks;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(name = "CategorySummary", description = "Resumen básico de una categoría")
    public static class CategorySummary {
        @Schema(description = "Identificador único de la categoría", example = "1")
        private Long categoryId;

        @Schema(description = "Nombre de la categoría", example = "Ciencia Ficción")
        private String categoryName;

        @Schema(description = "URL de la imagen que representa la categoría", example = "https://epilogo-bucket.s3.amazonaws.com/categories/sci-fi.jpg")
        private String imageUrl;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(name = "CategoryCreateRequest", description = "Datos requeridos para crear una nueva categoría")
    public static class CategoryCreateRequest {
        @NotBlank(message = "El nombre de la categoría es obligatorio")
        @Schema(description = "Nombre de la categoría", example = "Ciencia Ficción", required = true)
        private String categoryName;

        @Schema(description = "Descripción detallada de la categoría", example = "Libros que exploran futuros alternativos y tecnología avanzada")
        private String description;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(name = "CategoryUpdateRequest", description = "Datos para actualizar una categoría existente")
    public static class CategoryUpdateRequest {
        @Schema(description = "Nuevo nombre de la categoría", example = "Ciencia Ficción Moderna")
        private String categoryName;

        @Schema(description = "Nueva descripción de la categoría", example = "Literatura contemporánea que explora futuros alternativos")
        private String description;
    }
}