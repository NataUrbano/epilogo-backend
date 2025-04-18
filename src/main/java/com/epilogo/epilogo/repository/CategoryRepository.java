package com.epilogo.epilogo.repository;

import com.epilogo.epilogo.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends PagingAndSortingRepository<Category, Integer> {
    Optional<Category> findByCategoryName(String categoryName);
}
