package com.epilogo.epilogo.controller;

import com.epilogo.epilogo.dto.CategoryDTO;
import com.epilogo.epilogo.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@Tag(name = "Categorías", description = "API para gestión de categorías de libros")
@SecurityRequirement(name = "bearerAuth")
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping("/{categoryId}")
    @Operation(summary = "Obtener categoría por ID", description = "Devuelve información completa de una categoría y sus libros")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Categoría encontrada",
                    content = @Content(schema = @Schema(implementation = CategoryDTO.CategoryResponse.class))),
            @ApiResponse(responseCode = "404", description = "Categoría no encontrada", content = @Content)
    })
    public ResponseEntity<CategoryDTO.CategoryResponse> getCategoryById(
            @Parameter(description = "ID de la categoría", required = true, example = "1")
            @PathVariable Long categoryId) {
        return ResponseEntity.ok(categoryService.getCategoryById(categoryId));
    }

    @GetMapping
    @Operation(summary = "Listar todas las categorías", description = "Obtiene una lista resumida de todas las categorías")
    @ApiResponse(responseCode = "200", description = "Lista de categorías obtenida correctamente")
    public ResponseEntity<List<CategoryDTO.CategorySummary>> getAllCategories() {
        return ResponseEntity.ok(categoryService.getAllCategories());
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    @Operation(summary = "Crear categoría", description = "Crea una nueva categoría en el sistema")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Categoría creada correctamente",
                    content = @Content(schema = @Schema(implementation = CategoryDTO.CategoryResponse.class))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos", content = @Content),
            @ApiResponse(responseCode = "403", description = "No tiene permisos suficientes", content = @Content),
            @ApiResponse(responseCode = "409", description = "Ya existe una categoría con ese nombre", content = @Content)
    })
    public ResponseEntity<CategoryDTO.CategoryResponse> createCategory(
            @Parameter(description = "Datos de la categoría a crear", required = true)
            @RequestBody @Valid CategoryDTO.CategoryCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(categoryService.createCategory(request));
    }

    @PutMapping("/{categoryId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    @Operation(summary = "Actualizar categoría", description = "Actualiza los datos de una categoría existente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Categoría actualizada correctamente",
                    content = @Content(schema = @Schema(implementation = CategoryDTO.CategoryResponse.class))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos", content = @Content),
            @ApiResponse(responseCode = "403", description = "No tiene permisos suficientes", content = @Content),
            @ApiResponse(responseCode = "404", description = "Categoría no encontrada", content = @Content),
            @ApiResponse(responseCode = "409", description = "Ya existe una categoría con ese nombre", content = @Content)
    })
    public ResponseEntity<CategoryDTO.CategoryResponse> updateCategory(
            @Parameter(description = "ID de la categoría a actualizar", required = true, example = "1")
            @PathVariable Long categoryId,
            @Parameter(description = "Datos actualizados de la categoría", required = true)
            @RequestBody @Valid CategoryDTO.CategoryUpdateRequest request) {
        return ResponseEntity.ok(categoryService.updateCategory(categoryId, request));
    }

    @DeleteMapping("/{categoryId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    @Operation(summary = "Eliminar categoría", description = "Elimina una categoría si no tiene libros asociados")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Categoría eliminada correctamente"),
            @ApiResponse(responseCode = "400", description = "No se puede eliminar porque tiene libros asociados", content = @Content),
            @ApiResponse(responseCode = "403", description = "No tiene permisos suficientes", content = @Content),
            @ApiResponse(responseCode = "404", description = "Categoría no encontrada", content = @Content)
    })
    public ResponseEntity<Void> deleteCategory(
            @Parameter(description = "ID de la categoría a eliminar", required = true, example = "1")
            @PathVariable Long categoryId) {
        categoryService.deleteCategory(categoryId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping(value = "/{categoryId}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    @Operation(summary = "Subir imagen de categoría", description = "Sube una imagen para representar la categoría")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Imagen subida correctamente",
                    content = @Content(schema = @Schema(implementation = CategoryDTO.CategoryResponse.class))),
            @ApiResponse(responseCode = "400", description = "Archivo inválido", content = @Content),
            @ApiResponse(responseCode = "403", description = "No tiene permisos suficientes", content = @Content),
            @ApiResponse(responseCode = "404", description = "Categoría no encontrada", content = @Content)
    })
    public ResponseEntity<CategoryDTO.CategoryResponse> uploadCategoryImage(
            @Parameter(description = "ID de la categoría", required = true, example = "1")
            @PathVariable Long categoryId,
            @Parameter(description = "Archivo de imagen", required = true)
            @RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(categoryService.uploadCategoryImage(categoryId, file));
    }

    @GetMapping("/popular")
    @Operation(summary = "Obtener categorías más populares", description = "Obtiene las categorías más populares basado en reservas recientes")
    @ApiResponse(responseCode = "200", description = "Lista de categorías populares obtenida correctamente")
    public ResponseEntity<List<CategoryDTO.CategorySummary>> getMostPopularCategories(
            @Parameter(description = "Número máximo de categorías a devolver", example = "10")
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(categoryService.getMostPopularCategories(limit));
    }
}