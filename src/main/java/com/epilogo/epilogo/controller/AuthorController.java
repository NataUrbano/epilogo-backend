package com.epilogo.epilogo.controller;

import com.epilogo.epilogo.dto.AuthorDTO;
import com.epilogo.epilogo.service.AuthorService;
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
@RequestMapping("/api/authors")
@RequiredArgsConstructor
@Tag(name = "Autores", description = "API para gestión de autores")
@SecurityRequirement(name = "bearerAuth")
public class AuthorController {

    private final AuthorService authorService;

    @GetMapping("/{authorId}")
    @Operation(summary = "Obtener autor por ID", description = "Devuelve información completa de un autor y sus libros")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Autor encontrado",
                    content = @Content(schema = @Schema(implementation = AuthorDTO.AuthorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Autor no encontrado", content = @Content)
    })
    public ResponseEntity<AuthorDTO.AuthorResponse> getAuthorById(
            @Parameter(description = "ID del autor", required = true, example = "1")
            @PathVariable Long authorId) {
        return ResponseEntity.ok(authorService.getAuthorById(authorId));
    }

    @GetMapping("/search")
    @Operation(summary = "Buscar autores por nombre", description = "Busca autores que coincidan parcialmente con el nombre proporcionado")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Búsqueda realizada correctamente")
    })
    public ResponseEntity<Page<AuthorDTO.AuthorSummary>> findAuthorsByName(
            @Parameter(description = "Texto a buscar en los nombres de autores", required = true, example = "García")
            @RequestParam String name,
            @Parameter(description = "Número de página (desde 0)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamaño de página", example = "10")
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(authorService.findAuthorsByName(name, page, size));
    }

    @GetMapping
    @Operation(summary = "Listar todos los autores", description = "Obtiene una lista resumida de todos los autores")
    @ApiResponse(responseCode = "200", description = "Lista de autores obtenida correctamente")
    public ResponseEntity<List<AuthorDTO.AuthorSummary>> getAllAuthors() {
        return ResponseEntity.ok(authorService.getAllAuthors());
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    @Operation(summary = "Crear autor", description = "Crea un nuevo autor en el sistema")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Autor creado correctamente",
                    content = @Content(schema = @Schema(implementation = AuthorDTO.AuthorResponse.class))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos", content = @Content),
            @ApiResponse(responseCode = "403", description = "No tiene permisos suficientes", content = @Content),
            @ApiResponse(responseCode = "409", description = "Ya existe un autor con ese nombre", content = @Content)
    })
    public ResponseEntity<AuthorDTO.AuthorResponse> createAuthor(
            @Parameter(description = "Datos del autor a crear", required = true)
            @RequestBody @Valid AuthorDTO.AuthorCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authorService.createAuthor(request));
    }

    @PutMapping("/{authorId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    @Operation(summary = "Actualizar autor", description = "Actualiza los datos de un autor existente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Autor actualizado correctamente",
                    content = @Content(schema = @Schema(implementation = AuthorDTO.AuthorResponse.class))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos", content = @Content),
            @ApiResponse(responseCode = "403", description = "No tiene permisos suficientes", content = @Content),
            @ApiResponse(responseCode = "404", description = "Autor no encontrado", content = @Content),
            @ApiResponse(responseCode = "409", description = "Ya existe un autor con ese nombre", content = @Content)
    })
    public ResponseEntity<AuthorDTO.AuthorResponse> updateAuthor(
            @Parameter(description = "ID del autor a actualizar", required = true, example = "1")
            @PathVariable Long authorId,
            @Parameter(description = "Datos actualizados del autor", required = true)
            @RequestBody @Valid AuthorDTO.AuthorUpdateRequest request) {
        return ResponseEntity.ok(authorService.updateAuthor(authorId, request));
    }

    @DeleteMapping("/{authorId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    @Operation(summary = "Eliminar autor", description = "Elimina un autor si no tiene libros asociados")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Autor eliminado correctamente"),
            @ApiResponse(responseCode = "403", description = "No tiene permisos suficientes", content = @Content),
            @ApiResponse(responseCode = "404", description = "Autor no encontrado", content = @Content),
            @ApiResponse(responseCode = "400", description = "No se puede eliminar porque tiene libros asociados", content = @Content)
    })
    public ResponseEntity<Void> deleteAuthor(
            @Parameter(description = "ID del autor a eliminar", required = true, example = "1")
            @PathVariable Long authorId) {
        authorService.deleteAuthor(authorId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping(value = "/{authorId}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    @Operation(summary = "Subir imagen de autor", description = "Sube una imagen para el autor")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Imagen subida correctamente",
                    content = @Content(schema = @Schema(implementation = AuthorDTO.AuthorResponse.class))),
            @ApiResponse(responseCode = "400", description = "Archivo inválido", content = @Content),
            @ApiResponse(responseCode = "403", description = "No tiene permisos suficientes", content = @Content),
            @ApiResponse(responseCode = "404", description = "Autor no encontrado", content = @Content)
    })
    public ResponseEntity<AuthorDTO.AuthorResponse> uploadAuthorImage(
            @Parameter(description = "ID del autor", required = true, example = "1")
            @PathVariable Long authorId,
            @Parameter(description = "Archivo de imagen", required = true)
            @RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(authorService.uploadAuthorImage(authorId, file));
    }

    @GetMapping("/popular")
    @Operation(summary = "Obtener autores más populares", description = "Obtiene los autores más populares basado en reservas recientes")
    @ApiResponse(responseCode = "200", description = "Lista de autores populares obtenida correctamente")
    public ResponseEntity<List<AuthorDTO.AuthorSummary>> getMostPopularAuthors(
            @Parameter(description = "Número máximo de autores a devolver", example = "10")
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(authorService.getMostPopularAuthors(limit));
    }
}