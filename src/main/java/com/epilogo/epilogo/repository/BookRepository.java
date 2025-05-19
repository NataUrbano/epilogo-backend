package com.epilogo.epilogo.repository;

import com.epilogo.epilogo.model.Book;
import com.epilogo.epilogo.model.Book.BookStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;
import java.util.Optional;

@Repository
@Tag(name = "Book Repository", description = "Repositorio para operaciones con libros")
public interface BookRepository extends JpaRepository<Book, Long> {

    @Query("SELECT b FROM Book b LEFT JOIN FETCH b.author LEFT JOIN FETCH b.category WHERE b.bookId = :id")
    Optional<Book> findByIdWithDetails(@Param("id") Long id);

    Page<Book> findByTitleContainingIgnoreCase(String title, Pageable pageable);

    Page<Book> findByCategoryCategoryId(Long categoryId, Pageable pageable);

    Page<Book> findByAuthorAuthorId(Long authorId, Pageable pageable);

    Page<Book> findByBookStatus(BookStatus status, Pageable pageable);

    @Query("SELECT b FROM Book b WHERE b.availableAmount = 0")
    List<Book> findUnavailableBooks();

    @Query("SELECT b FROM Book b WHERE b.availableAmount < (b.totalAmount * 0.2)")
    List<Book> findLowStockBooks();

    @Query(value = "SELECT b.* FROM books b " +
            "JOIN reservations r ON b.book_id = r.book_id " +
            "WHERE r.reservation_date >= DATE_SUB(CURRENT_DATE, INTERVAL 30 DAY) " +
            "GROUP BY b.book_id " +
            "ORDER BY COUNT(r.reservation_id) DESC LIMIT :limit", nativeQuery = true)
    List<Book> findMostReservedBooks(@Param("limit") int limit);

    @Query("SELECT b FROM Book b WHERE " +
            "LOWER(b.title) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(b.author.authorName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(b.category.categoryName) LIKE LOWER(CONCAT('%', :query, '%'))")
    Page<Book> searchBooks(@Param("query") String query, Pageable pageable);
}