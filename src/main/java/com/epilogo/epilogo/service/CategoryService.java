package com.epilogo.epilogo.service;

import com.epilogo.epilogo.dto.BookDTO;
import com.epilogo.epilogo.dto.CategoryDTO;
import com.epilogo.epilogo.exception.ResourceNotFoundException;
import com.epilogo.epilogo.model.Book;
import com.epilogo.epilogo.model.Category;
import com.epilogo.epilogo.model.S3File;
import com.epilogo.epilogo.repository.CategoryRepository;
import com.epilogo.epilogo.repository.S3FileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final S3Service s3Service;
    private final S3FileRepository s3FileRepository;

    /**
     * Get a category by ID with all books
     */
    public CategoryDTO.CategoryResponse getCategoryById(Long categoryId) {
        Category category = categoryRepository.findByIdWithBooks(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada con ID: " + categoryId));

        return mapToCategoryResponse(category);
    }

    /**
     * Get all categories
     */
    public List<CategoryDTO.CategorySummary> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(this::mapToCategorySummary)
                .collect(Collectors.toList());
    }

    /**
     * Create a new category
     */
    @Transactional
    public CategoryDTO.CategoryResponse createCategory(CategoryDTO.CategoryCreateRequest request) {
        // Check if category name already exists
        if (categoryRepository.existsByCategoryNameIgnoreCase(request.getCategoryName())) {
            throw new IllegalStateException("Ya existe una categoría con ese nombre");
        }

        // Create category
        Category category = Category.builder()
                .categoryName(request.getCategoryName())
                .description(request.getDescription())
                .build();

        // Save category
        Category savedCategory = categoryRepository.save(category);

        return mapToCategoryResponse(savedCategory);
    }

    /**
     * Update a category
     */
    @Transactional
    public CategoryDTO.CategoryResponse updateCategory(Long categoryId, CategoryDTO.CategoryUpdateRequest request) {
        // Get category
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada con ID: " + categoryId));

        // Check if new name exists only if name is changing
        if (request.getCategoryName() != null && !request.getCategoryName().equals(category.getCategoryName())) {
            if (categoryRepository.existsByCategoryNameIgnoreCase(request.getCategoryName())) {
                throw new IllegalStateException("Ya existe una categoría con ese nombre");
            }
            category.setCategoryName(request.getCategoryName());
        }

        // Update description if provided
        if (request.getDescription() != null) {
            category.setDescription(request.getDescription());
        }

        // Save updated category
        Category updatedCategory = categoryRepository.save(category);

        return mapToCategoryResponse(updatedCategory);
    }

    /**
     * Delete a category
     */
    @Transactional
    public void deleteCategory(Long categoryId) {
        // Check if category exists
        Category category = categoryRepository.findByIdWithBooks(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada con ID: " + categoryId));

        // Check if category has books
        if (!category.getBooks().isEmpty()) {
            throw new IllegalStateException("No se puede eliminar la categoría porque tiene libros asociados");
        }

        // Delete category
        categoryRepository.deleteById(categoryId);
    }

    /**
     * Upload category image
     */
    @Transactional
    public CategoryDTO.CategoryResponse uploadCategoryImage(Long categoryId, MultipartFile file) {
        // Get category
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada con ID: " + categoryId));

        // Upload file to S3
        S3File s3File = s3Service.uploadFile(file, S3File.EntityType.CATEGORY, categoryId);

        // Update category with image URL
        category.setImageUrl(s3File.getS3Url());
        Category updatedCategory = categoryRepository.save(category);

        return mapToCategoryResponse(updatedCategory);
    }

    /**
     * Get most popular categories
     */
    public List<CategoryDTO.CategorySummary> getMostPopularCategories(int limit) {
        return categoryRepository.findMostPopularCategories(limit).stream()
                .map(this::mapToCategorySummary)
                .collect(Collectors.toList());
    }

    /**
     * Helper method to map Category entity to CategoryResponse DTO
     */
    private CategoryDTO.CategoryResponse mapToCategoryResponse(Category category) {
        // Asegurarse de que category.getBooks() no sea null
        List<Book> books = category.getBooks() == null ? Collections.emptyList() : category.getBooks();

        Optional<S3File> latestFileOpt = s3FileRepository.findFirstByEntityIdAndEntityTypeOrderByUploadDateDesc(category.getCategoryId(), S3File.EntityType.CATEGORY);

        String imageUrl = latestFileOpt.map(S3File::getS3Url).orElse(null);

        // Crear la respuesta DTO
        return CategoryDTO.CategoryResponse.builder()
                .categoryId(category.getCategoryId())
                .categoryName(category.getCategoryName())
                .description(category.getDescription())
                .imageUrl(imageUrl)  // Usar la URL guardada
                .books(books.stream()
                        .map(book -> BookDTO.BookSummary.builder()
                                .bookId(book.getBookId())
                                .title(book.getTitle())
                                .imageUrl(book.getImageUrl())
                                .bookStatus(book.getBookStatus())
                                .authorName(book.getAuthor().getAuthorName())
                                .build())
                        .collect(Collectors.toList()))
                .totalBooks(books.size())
                .build();
    }

    /**
     * Helper method to map Category entity to CategorySummary DTO
     */
    private CategoryDTO.CategorySummary mapToCategorySummary(Category category) {
        Optional<S3File> latestFileOpt = s3FileRepository.findFirstByEntityIdAndEntityTypeOrderByUploadDateDesc(category.getCategoryId(), S3File.EntityType.CATEGORY);

        String imageUrl = latestFileOpt.map(S3File::getS3Url).orElse(null);

        return CategoryDTO.CategorySummary.builder()
                .categoryId(category.getCategoryId())
                .categoryName(category.getCategoryName())
                .imageUrl(imageUrl)  // Usar la URL guardada
                .build();
    }
}