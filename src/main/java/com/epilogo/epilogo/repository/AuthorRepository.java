package com.epilogo.epilogo.repository;

import com.epilogo.epilogo.model.Author;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AuthorRepository extends JpaRepository<Author, Long> {

    Optional<Author> findByAuthorNameIgnoreCase(String authorName);

    @Query("SELECT DISTINCT a FROM Author a LEFT JOIN FETCH a.books WHERE a.authorId = :id")
    Optional<Author> findByIdWithBooks(@Param("id") Long id);

    Page<Author> findByAuthorNameContainingIgnoreCase(String name, Pageable pageable);

    boolean existsByAuthorNameIgnoreCase(String authorName);

    @Query(value = "SELECT a.* FROM authors a " +
            "JOIN books b ON a.author_id = b.author_id " +
            "JOIN reservations r ON b.book_id = r.book_id " +
            "WHERE r.reservation_date >= DATE_SUB(CURRENT_DATE, INTERVAL 90 DAY) " +
            "GROUP BY a.author_id " +
            "ORDER BY COUNT(r.reservation_id) DESC LIMIT :limit", nativeQuery = true)
    List<Author> findMostPopularAuthors(@Param("limit") int limit);
}