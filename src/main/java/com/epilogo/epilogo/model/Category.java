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
@Table(name = "categories")
@Schema(description = "Entidad que representa una categoría de libros")
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_id")
    @Schema(description = "Identificador único de la categoría", example = "1")
    private Long categoryId;

    @Column(name = "category_name", nullable = false, unique = true, length = 50)
    @Schema(description = "Nombre de la categoría", example = "Ciencia Ficción", required = true)
    private String categoryName;

    @Column(name = "description", columnDefinition = "TEXT")
    @Schema(description = "Descripción detallada de la categoría", example = "Libros de ciencia ficción que exploran futuros alternativos y tecnología avanzada")
    private String description;

    @Transient
    @Schema(description = "URL de la imagen de la categoría", example = "https://epilogo-bucket.s3.amazonaws.com/categories/sci-fi.jpg")
    private String imageUrl;

    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference("category-books")
    @Schema(description = "Lista de libros que pertenecen a esta categoría")
    private List<Book> books = new ArrayList<>();

    @PrePersist
    @PreUpdate
    public void prepareData() {
        this.categoryName = this.categoryName != null ? this.categoryName.trim() : null;
    }

    @Override
    public String toString() {
        return "Category{" +
                "categoryId=" + categoryId +
                ", categoryName='" + categoryName + '\'' +
                '}';
    }
}