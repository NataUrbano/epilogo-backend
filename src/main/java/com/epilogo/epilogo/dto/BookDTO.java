package com.epilogo.epilogo.dto;

import com.epilogo.epilogo.model.Book;
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
    public static class BookResponse {
        private Long bookId;
        private String title;
        private String description;
        private String isbn;
        private AuthorDTO.AuthorSummary author;
        private CategoryDTO.CategorySummary category;
        private Integer totalAmount;
        private Integer availableAmount;
        private Book.BookStatus bookStatus;
        private String imageUrl;
        private LocalDateTime registerDate;
        private Integer publicationYear;
        private Integer activeReservations;
        private Boolean isReservedByCurrentUser;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BookSummary {
        private Long bookId;
        private String title;
        private String imageUrl;
        private Book.BookStatus bookStatus;
        private String authorName;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BookCreateRequest {
        @NotBlank(message = "El título es obligatorio")
        private String title;

        private String description;

        private String isbn;

        @NotNull(message = "El autor es obligatorio")
        private Long authorId;

        @NotNull(message = "La categoría es obligatoria")
        private Long categoryId;

        @NotNull(message = "La cantidad total es obligatoria")
        @Min(value = 1, message = "La cantidad total debe ser al menos 1")
        private Integer totalAmount;

        @NotNull(message = "La cantidad disponible es obligatoria")
        @Min(value = 0, message = "La cantidad disponible no puede ser negativa")
        private Integer availableAmount;

        private Integer publicationYear;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BookUpdateRequest {
        private String title;
        private String description;
        private String isbn;
        private Long authorId;
        private Long categoryId;
        private Integer totalAmount;
        private Integer availableAmount;
        private Integer publicationYear;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BookSearchRequest {
        private String query;
        private Long categoryId;
        private Long authorId;
        private Book.BookStatus status;
        private Integer page;
        private Integer size;
        private String sortBy;
        private String sortDirection;
    }
}