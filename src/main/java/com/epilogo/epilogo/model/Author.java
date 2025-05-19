package com.epilogo.epilogo.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "authors")
@Schema(description = "Entidad que representa un autor de libros")
public class Author {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "author_id")
    @Schema(description = "Identificador único del autor", example = "1")
    private Long authorId;

    @Column(name = "author_name", nullable = false, length = 100)
    @Schema(description = "Nombre completo del autor", example = "Gabriel García Márquez", required = true)
    private String authorName;

    @Column(name = "biography", columnDefinition = "TEXT")
    @Schema(description = "Biografía del autor", example = "Gabriel García Márquez fue un escritor, novelista, cuentista, guionista y periodista colombiano...")
    private String biography;

    @Column(name = "birth_year")
    @Schema(description = "Año de nacimiento del autor", example = "1927")
    private Integer birthYear;

    @Column(name = "death_year")
    @Schema(description = "Año de fallecimiento del autor (si aplica)", example = "2014")
    private Integer deathYear;

    @Transient
    @Schema(description = "URL de la imagen del autor", example = "https://epilogo-bucket.s3.amazonaws.com/authors/gabriel-garcia-marquez.jpg")
    private String imageUrl;

    @OneToMany(mappedBy = "author", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference("author-books")
    @Schema(description = "Lista de libros escritos por este autor")
    private List<Book> books = new ArrayList<>();

    @PrePersist
    @PreUpdate
    public void prepareData() {
        this.authorName = this.authorName != null ? this.authorName.trim() : null;
    }

    @Override
    public String toString() {
        return "Author{" +
                "authorId=" + authorId +
                ", authorName='" + authorName + '\'' +
                ", birthYear=" + birthYear +
                ", deathYear=" + deathYear +
                '}';
    }
}