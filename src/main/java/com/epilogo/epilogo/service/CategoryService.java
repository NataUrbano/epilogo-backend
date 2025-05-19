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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Tag(name = "Category Service", description = "Servicio para gestionar categorías de libros")
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final S3Service s3Service;
    private final S3FileRepository s3FileRepository;

    @Operation(summary = "Obtener categoría por ID", description = "Obtiene una categoría con todos sus libros por su ID")
    public CategoryDTO.CategoryResponse getCategoryById(Long categoryId) {
        Category category = categoryRepository.findByIdWithBooks(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada con ID: " + categoryId));

        return mapToCategoryResponse(category);
    }

    @Operation(summary = "Obtener todas las categorías", description = "Obtiene una lista resumida de todas las categorías")
    public List<CategoryDTO.CategorySummary> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(this::mapToCategorySummary)
                .collect(Collectors.toList());
    }

    @Transactional
    @Operation(summary = "Crear categoría", description = "Crea una nueva categoría en el sistema")
    public CategoryDTO.CategoryResponse createCategory(CategoryDTO.CategoryCreateRequest request) {
        if (categoryRepository.existsByCategoryNameIgnoreCase(request.getCategoryName())) {
            throw new IllegalStateException("Ya existe una categoría con ese nombre");
        }

        Category category = Category.builder()
                .categoryName(request.getCategoryName())
                .description(request.getDescription())
                .build();

        Category savedCategory = categoryRepository.save(category);

        return mapToCategoryResponse(savedCategory);
    }

    @Transactional
    @Operation(summary = "Actualizar categoría", description = "Actualiza los datos de una categoría existente")
    public CategoryDTO.CategoryResponse updateCategory(Long categoryId, CategoryDTO.CategoryUpdateRequest request) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada con ID: " + categoryId));

        if (request.getCategoryName() != null && !request.getCategoryName().equals(category.getCategoryName())) {
            if (categoryRepository.existsByCategoryNameIgnoreCase(request.getCategoryName())) {
                throw new IllegalStateException("Ya existe una categoría con ese nombre");
            }
            category.setCategoryName(request.getCategoryName());
        }

        if (request.getDescription() != null) {
            category.setDescription(request.getDescription());
        }

        Category updatedCategory = categoryRepository.save(category);

        return mapToCategoryResponse(updatedCategory);
    }

    @Transactional
    @Operation(summary = "Eliminar categoría", description = "Elimina una categoría si no tiene libros asociados")
    public void deleteCategory(Long categoryId) {
        Category category = categoryRepository.findByIdWithBooks(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada con ID: " + categoryId));

        if (!category.getBooks().isEmpty()) {
            throw new IllegalStateException("No se puede eliminar la categoría porque tiene libros asociados");
        }

        categoryRepository.deleteById(categoryId);
    }

    @Transactional
    @Operation(summary = "Subir imagen de categoría", description = "Sube una imagen para representar la categoría")
    public CategoryDTO.CategoryResponse uploadCategoryImage(Long categoryId, MultipartFile file) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada con ID: " + categoryId));

        S3File s3File = s3Service.uploadFile(file, S3File.EntityType.CATEGORY, categoryId);

        category.setImageUrl(s3File.getS3Url());
        Category updatedCategory = categoryRepository.save(category);

        return mapToCategoryResponse(updatedCategory);
    }

    @Operation(summary = "Obtener categorías más populares", description = "Obtiene las categorías más populares según las reservas recientes")
    public List<CategoryDTO.CategorySummary> getMostPopularCategories(int limit) {
        return categoryRepository.findMostPopularCategories(limit).stream()
                .map(this::mapToCategorySummary)
                .collect(Collectors.toList());
    }

    private CategoryDTO.CategoryResponse mapToCategoryResponse(Category category) {
        List<Book> books = category.getBooks() == null ? Collections.emptyList() : category.getBooks();

        Optional<S3File> latestFileOpt = s3FileRepository.findFirstByEntityIdAndEntityTypeOrderByUploadDateDesc(category.getCategoryId(), S3File.EntityType.CATEGORY);

        String imageUrl = latestFileOpt.map(S3File::getS3Url).orElse(null);

        return CategoryDTO.CategoryResponse.builder()
                .categoryId(category.getCategoryId())
                .categoryName(category.getCategoryName())
                .description(category.getDescription())
                .imageUrl(imageUrl)
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

    private CategoryDTO.CategorySummary mapToCategorySummary(Category category) {
        Optional<S3File> latestFileOpt = s3FileRepository.findFirstByEntityIdAndEntityTypeOrderByUploadDateDesc(category.getCategoryId(), S3File.EntityType.CATEGORY);

        String imageUrl = latestFileOpt.map(S3File::getS3Url).orElse(null);

        return CategoryDTO.CategorySummary.builder()
                .categoryId(category.getCategoryId())
                .categoryName(category.getCategoryName())
                .imageUrl(imageUrl)
                .build();
    }
}