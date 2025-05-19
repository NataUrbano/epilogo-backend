package com.epilogo.epilogo.dto;

import com.epilogo.epilogo.model.Book;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

public class BookDTO {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(name = "BookResponse", description = "Respuesta con los detalles completos de un libro")
    public static class BookResponse {
        @Schema(description = "Identificador único del libro", example = "789")
        private Long bookId;

        @Schema(description = "Título del libro", example = "Cien Años de Soledad")
        private String title;

        @Schema(description = "Descripción del libro", example = "Una novela del realismo mágico escrita por Gabriel García Márquez.")
        private String description;

        @Schema(description = "Código ISBN del libro", example = "978-3-16-148410-0")
        private String isbn;

        @Schema(description = "Resumen del autor")
        private AuthorDTO.AuthorSummary author;

        @Schema(description = "Resumen de la categoría")
        private CategoryDTO.CategorySummary category;

        @Schema(description = "Cantidad total de ejemplares", example = "10")
        private Integer totalAmount;

        @Schema(description = "Cantidad de ejemplares disponibles", example = "7")
        private Integer availableAmount;

        @Schema(description = "Estado actual del libro", example = "AVAILABLE")
        private Book.BookStatus bookStatus;

        @Schema(description = "URL de la imagen del libro", example = "https://example.com/images/book123.jpg")
        private String imageUrl;

        @Schema(description = "Fecha de registro del libro", example = "2024-05-18T16:50:00", format = "date-time")
        private LocalDateTime registerDate;

        @Schema(description = "Año de publicación", example = "1967")
        private Integer publicationYear;

        @Schema(description = "Cantidad de reservas activas para este libro", example = "3")
        private Integer activeReservations;

        @Schema(description = "Indica si el libro está reservado por el usuario actual", example = "false")
        private Boolean isReservedByCurrentUser;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(name = "BookSummary", description = "Resumen básico de un libro")
    public static class BookSummary {
        @Schema(description = "Identificador único del libro", example = "789")
        private Long bookId;

        @Schema(description = "Título del libro", example = "Cien Años de Soledad")
        private String title;

        @Schema(description = "URL de la imagen del libro", example = "https://example.com/images/book123.jpg")
        private String imageUrl;

        @Schema(description = "Estado actual del libro", example = "AVAILABLE")
        private Book.BookStatus bookStatus;

        @Schema(description = "Nombre del autor", example = "Gabriel García Márquez")
        private String authorName;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(name = "BookCreateRequest", description = "Datos para crear un nuevo libro")
    public static class BookCreateRequest {
        @NotBlank(message = "El título es obligatorio")
        @Schema(description = "Título del libro", example = "Cien Años de Soledad", required = true)
        private String title;

        @Schema(description = "Descripción del libro", example = "Una novela del realismo mágico escrita por Gabriel García Márquez.")
        private String description;

        @Schema(description = "Código ISBN del libro", example = "978-3-16-148410-0")
        private String isbn;

        @NotNull(message = "El autor es obligatorio")
        @Schema(description = "ID del autor", example = "12", required = true)
        private Long authorId;

        @NotNull(message = "La categoría es obligatoria")
        @Schema(description = "ID de la categoría", example = "5", required = true)
        private Long categoryId;

        @NotNull(message = "La cantidad total es obligatoria")
        @Min(value = 1, message = "La cantidad total debe ser al menos 1")
        @Schema(description = "Cantidad total de ejemplares", example = "10", required = true)
        private Integer totalAmount;

        @NotNull(message = "La cantidad disponible es obligatoria")
        @Min(value = 0, message = "La cantidad disponible no puede ser negativa")
        @Schema(description = "Cantidad de ejemplares disponibles", example = "7", required = true)
        private Integer availableAmount;

        @Schema(description = "Año de publicación", example = "1967")
        private Integer publicationYear;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(name = "BookUpdateRequest", description = "Datos para actualizar un libro existente")
    public static class BookUpdateRequest {
        @Schema(description = "Título del libro", example = "Cien Años de Soledad")
        private String title;

        @Schema(description = "Descripción del libro", example = "Una novela del realismo mágico escrita por Gabriel García Márquez.")
        private String description;

        @Schema(description = "Código ISBN del libro", example = "978-3-16-148410-0")
        private String isbn;

        @Schema(description = "ID del autor", example = "12")
        private Long authorId;

        @Schema(description = "ID de la categoría", example = "5")
        private Long categoryId;

        @Schema(description = "Cantidad total de ejemplares", example = "10")
        private Integer totalAmount;

        @Schema(description = "Cantidad de ejemplares disponibles", example = "7")
        private Integer availableAmount;

        @Schema(description = "Año de publicación", example = "1967")
        private Integer publicationYear;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(name = "BookSearchRequest", description = "Parámetros para buscar libros")
    public static class BookSearchRequest {
        @Schema(description = "Texto para búsqueda en título o descripción", example = "realismo mágico")
        private String query;

        @Schema(description = "ID de la categoría para filtrar")
        private Long categoryId;

        @Schema(description = "ID del autor para filtrar")
        private Long authorId;

        @Schema(description = "Estado del libro para filtrar", example = "AVAILABLE")
        private Book.BookStatus status;

        @Schema(description = "Número de página para paginación", example = "0")
        private Integer page;

        @Schema(description = "Tamaño de página para paginación", example = "10")
        private Integer size;

        @Schema(description = "Campo por el cual ordenar", example = "title")
        private String sortBy;

        @Schema(description = "Dirección del ordenamiento (asc o desc)", example = "asc")
        private String sortDirection;
    }
}
