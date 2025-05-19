package com.epilogo.epilogo.repository;

import com.epilogo.epilogo.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;
import java.util.Optional;

@Repository
@Tag(name = "Category Repository", description = "Repositorio para operaciones con categorías de libros")
public interface CategoryRepository extends JpaRepository<Category, Long> {

    Optional<Category> findByCategoryNameIgnoreCase(String categoryName);

    boolean existsByCategoryNameIgnoreCase(String categoryName);

    @Query("SELECT c FROM Category c LEFT JOIN FETCH c.books WHERE c.categoryId = :id")
    Optional<Category> findByIdWithBooks(@Param("id") Long id);

    @Query(value = "SELECT c.*, COUNT(b.book_id) as book_count FROM categories c " +
            "LEFT JOIN books b ON c.category_id = b.category_id " +
            "GROUP BY c.category_id " +
            "ORDER BY book_count DESC", nativeQuery = true)
    List<Category> findAllOrderByBookCount();

    @Query(value = "SELECT c.* FROM categories c " +
            "JOIN books b ON c.category_id = b.category_id " +
            "JOIN reservations r ON b.book_id = r.book_id " +
            "WHERE r.reservation_date >= DATE_SUB(CURRENT_DATE, INTERVAL 30 DAY) " +
            "GROUP BY c.category_id " +
            "ORDER BY COUNT(r.reservation_id) DESC LIMIT :limit", nativeQuery = true)
    List<Category> findMostPopularCategories(@Param("limit") int limit);
}