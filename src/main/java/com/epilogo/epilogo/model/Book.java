package com.epilogo.epilogo.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "books")
@Schema(description = "Entidad que representa un libro en el catálogo de la biblioteca")
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "book_id")
    @Schema(description = "Identificador único del libro", example = "1")
    private Long bookId;

    @Column(name = "title", nullable = false, length = 200)
    @Schema(description = "Título del libro", example = "Cien años de soledad", required = true)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    @Schema(description = "Descripción o sinopsis del libro", example = "Una saga familiar que narra la historia de la familia Buendía a lo largo de siete generaciones...")
    private String description;

    @Column(name = "isbn", length = 20)
    @Schema(description = "Número ISBN del libro", example = "978-0307474728")
    private String isbn;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    @JsonBackReference("author-books")
    @Schema(description = "Autor del libro", required = true)
    private Author author;

    @Column(name = "total_amount", nullable = false)
    @Schema(description = "Cantidad total de ejemplares del libro", example = "10", required = true)
    private Integer totalAmount;

    @Column(name = "available_amount", nullable = false)
    @Schema(description = "Cantidad de ejemplares disponibles para préstamo", example = "5", required = true)
    private Integer availableAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "book_status", nullable = false)
    @Schema(description = "Estado de disponibilidad del libro", example = "AVAILABLE", required = true)
    private BookStatus bookStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    @JsonBackReference("category-books")
    @Schema(description = "Categoría a la que pertenece el libro", required = true)
    private Category category;

    @Transient
    @Schema(description = "URL de la imagen de portada del libro", example = "https://epilogo-bucket.s3.amazonaws.com/books/cien-anos-soledad.jpg")
    private String imageUrl;

    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference("book-reservations")
    @Schema(description = "Lista de reservas asociadas al libro")
    private List<Reservation> reservations = new ArrayList<>();

    @Column(name = "register_date")
    @CreationTimestamp
    @Schema(description = "Fecha y hora de registro del libro en el sistema", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime registerDate;

    @Column(name = "publication_year")
    @Schema(description = "Año de publicación del libro", example = "1967")
    private Integer publicationYear;

    @Schema(description = "Estados posibles de disponibilidad de un libro")
    public enum BookStatus {
        @Schema(description = "Libro disponible para préstamo")
        AVAILABLE,
        @Schema(description = "Pocas copias disponibles (menos del 20% del total)")
        LOW_STOCK,
        @Schema(description = "No hay copias disponibles para préstamo")
        UNAVAILABLE
    }

    @PrePersist
    @PreUpdate
    public void updateBookStatus() {
        if (availableAmount <= 0) {
            this.bookStatus = BookStatus.UNAVAILABLE;
        } else if (availableAmount < totalAmount * 0.2) {
            this.bookStatus = BookStatus.LOW_STOCK;
        } else {
            this.bookStatus = BookStatus.AVAILABLE;
        }
    }

    @Override
    public String toString() {
        return "Book{" +
                "bookId=" + bookId +
                ", title='" + title + '\'' +
                ", isbn='" + isbn + '\'' +
                ", totalAmount=" + totalAmount +
                ", availableAmount=" + availableAmount +
                ", bookStatus=" + bookStatus +
                ", registerDate=" + registerDate +
                ", publicationYear=" + publicationYear +
                '}';
    }

    public void setAvailableAmount(Integer availableAmount) {
        this.availableAmount = availableAmount;
        updateBookStatus();
    }
}