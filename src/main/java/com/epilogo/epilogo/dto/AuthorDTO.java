package com.epilogo.epilogo.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

public class AuthorDTO {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AuthorResponse {
        private Long authorId;
        private String authorName;
        private String biography;
        private Integer birthYear;
        private Integer deathYear;
        private String imageUrl;
        private List<BookDTO.BookSummary> books;
        private Integer totalBooks;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AuthorSummary {
        private Long authorId;
        private String authorName;
        private String imageUrl;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AuthorCreateRequest {
        @NotBlank(message = "El nombre del autor es obligatorio")
        private String authorName;

        private String biography;
        private Integer birthYear;
        private Integer deathYear;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AuthorUpdateRequest {
        private String authorName;
        private String biography;
        private Integer birthYear;
        private Integer deathYear;
    }
}