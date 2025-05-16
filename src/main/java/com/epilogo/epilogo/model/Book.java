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
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "book_id")
    private Long bookId;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "isbn", length = 20)
    private String isbn;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    @JsonBackReference("author-books")
    private Author author;

    @Column(name = "total_amount", nullable = false)
    private Integer totalAmount;

    @Column(name = "available_amount", nullable = false)
    private Integer availableAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "book_status", nullable = false)
    private BookStatus bookStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    @JsonBackReference("category-books")
    private Category category;

    @Transient
    private String imageUrl;

    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference("book-reservations")
    private List<Reservation> reservations = new ArrayList<>();

    @Column(name = "register_date")
    @CreationTimestamp
    private LocalDateTime registerDate;

    @Column(name = "publication_year")
    private Integer publicationYear;

    public enum BookStatus {
        AVAILABLE, LOW_STOCK, UNAVAILABLE
    }

    /**
     * Actualiza el estado del libro basándose en available_amount
     */
    @PrePersist
    @PreUpdate
    public void updateBookStatus() {
        if (availableAmount <= 0) {
            this.bookStatus = BookStatus.UNAVAILABLE;
        } else if (availableAmount < totalAmount * 0.2) { // Less than 20% of copies available
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



    // Setter personalizado para availableAmount que actualiza el status automáticamente
    public void setAvailableAmount(Integer availableAmount) {
        this.availableAmount = availableAmount;
        updateBookStatus();
    }
}