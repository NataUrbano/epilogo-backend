package com.epilogo.epilogo.controller;

import com.epilogo.epilogo.dto.BookDTO;
import com.epilogo.epilogo.service.BookService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
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
@RequestMapping("/api/books")
@RequiredArgsConstructor
@Tag(name = "Libros", description = "API para gestión del catálogo de libros")
@SecurityRequirement(name = "bearerAuth")
public class BookController {

    private final BookService bookService;

    @GetMapping("/{bookId}")
    @Operation(summary = "Obtener libro por ID", description = "Devuelve información completa de un libro")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Libro encontrado",
                    content = @Content(schema = @Schema(implementation = BookDTO.BookResponse.class))),
            @ApiResponse(responseCode = "404", description = "Libro no encontrado", content = @Content)
    })
    public ResponseEntity<BookDTO.BookResponse> getBookById(
            @Parameter(description = "ID del libro", required = true, example = "1")
            @PathVariable Long bookId) {
        return ResponseEntity.ok(bookService.getBookById(bookId));
    }

    @GetMapping
    @Operation(summary = "Buscar libros", description = "Busca libros según diferentes criterios con paginación")
    @ApiResponse(responseCode = "200", description = "Búsqueda realizada correctamente")
    public ResponseEntity<Page<BookDTO.BookResponse>> searchBooks(
            @Parameter(description = "Criterios de búsqueda")
            @ModelAttribute BookDTO.BookSearchRequest request) {
        return ResponseEntity.ok(bookService.searchBooks(request));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    @Operation(summary = "Crear libro", description = "Crea un nuevo libro en el catálogo")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Libro creado correctamente",
                    content = @Content(schema = @Schema(implementation = BookDTO.BookResponse.class))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos", content = @Content),
            @ApiResponse(responseCode = "403", description = "No tiene permisos suficientes", content = @Content),
            @ApiResponse(responseCode = "404", description = "Autor o categoría no encontrados", content = @Content)
    })
    public ResponseEntity<BookDTO.BookResponse> createBook(
            @Parameter(description = "Datos del libro a crear", required = true)
            @RequestBody @Valid BookDTO.BookCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(bookService.createBook(request));
    }

    @PutMapping("/{bookId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    @Operation(summary = "Actualizar libro", description = "Actualiza los datos de un libro existente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Libro actualizado correctamente",
                    content = @Content(schema = @Schema(implementation = BookDTO.BookResponse.class))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos", content = @Content),
            @ApiResponse(responseCode = "403", description = "No tiene permisos suficientes", content = @Content),
            @ApiResponse(responseCode = "404", description = "Libro, autor o categoría no encontrados", content = @Content)
    })
    public ResponseEntity<BookDTO.BookResponse> updateBook(
            @Parameter(description = "ID del libro a actualizar", required = true, example = "1")
            @PathVariable Long bookId,
            @Parameter(description = "Datos actualizados del libro", required = true)
            @RequestBody @Valid BookDTO.BookUpdateRequest request) {
        return ResponseEntity.ok(bookService.updateBook(bookId, request));
    }

    @DeleteMapping("/{bookId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    @Operation(summary = "Eliminar libro", description = "Elimina un libro si no tiene reservas activas")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Libro eliminado correctamente"),
            @ApiResponse(responseCode = "400", description = "No se puede eliminar porque tiene reservas activas", content = @Content),
            @ApiResponse(responseCode = "403", description = "No tiene permisos suficientes", content = @Content),
            @ApiResponse(responseCode = "404", description = "Libro no encontrado", content = @Content)
    })
    public ResponseEntity<Void> deleteBook(
            @Parameter(description = "ID del libro a eliminar", required = true, example = "1")
            @PathVariable Long bookId) {
        bookService.deleteBook(bookId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping(value = "/{bookId}/cover", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    @Operation(summary = "Subir portada del libro", description = "Sube una imagen para la portada del libro")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Imagen subida correctamente",
                    content = @Content(schema = @Schema(implementation = BookDTO.BookResponse.class))),
            @ApiResponse(responseCode = "400", description = "Archivo inválido", content = @Content),
            @ApiResponse(responseCode = "403", description = "No tiene permisos suficientes", content = @Content),
            @ApiResponse(responseCode = "404", description = "Libro no encontrado", content = @Content)
    })
    public ResponseEntity<BookDTO.BookResponse> uploadBookCover(
            @Parameter(description = "ID del libro", required = true, example = "1")
            @PathVariable Long bookId,
            @Parameter(description = "Archivo de imagen", required = true)
            @RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(bookService.uploadBookCover(bookId, file));
    }

    @GetMapping("/popular")
    @Operation(summary = "Obtener libros más populares", description = "Obtiene los libros más reservados recientemente")
    @ApiResponse(responseCode = "200", description = "Lista de libros populares obtenida correctamente")
    public ResponseEntity<List<BookDTO.BookSummary>> getMostPopularBooks(
            @Parameter(description = "Número máximo de libros a devolver", example = "10")
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(bookService.getMostPopularBooks(limit));
    }
}