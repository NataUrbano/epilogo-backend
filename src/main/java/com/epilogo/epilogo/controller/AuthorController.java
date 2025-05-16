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

import java.util.List;

@RestController
@RequestMapping("/api/authors")
@RequiredArgsConstructor
public class AuthorController {

    private final AuthorService authorService;

    @GetMapping("/{authorId}")
    public ResponseEntity<AuthorDTO.AuthorResponse> getAuthorById(@PathVariable Long authorId) {
        return ResponseEntity.ok(authorService.getAuthorById(authorId));
    }

    @GetMapping("/search")
    public ResponseEntity<Page<AuthorDTO.AuthorSummary>> findAuthorsByName(
            @RequestParam String name,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(authorService.findAuthorsByName(name, page, size));
    }

    @GetMapping
    public ResponseEntity<List<AuthorDTO.AuthorSummary>> getAllAuthors() {
        return ResponseEntity.ok(authorService.getAllAuthors());
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public ResponseEntity<AuthorDTO.AuthorResponse> createAuthor(
            @RequestBody @Valid AuthorDTO.AuthorCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authorService.createAuthor(request));
    }

    @PutMapping("/{authorId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public ResponseEntity<AuthorDTO.AuthorResponse> updateAuthor(
            @PathVariable Long authorId,
            @RequestBody @Valid AuthorDTO.AuthorUpdateRequest request) {
        return ResponseEntity.ok(authorService.updateAuthor(authorId, request));
    }

    @DeleteMapping("/{authorId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public ResponseEntity<Void> deleteAuthor(@PathVariable Long authorId) {
        authorService.deleteAuthor(authorId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping(value = "/{authorId}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public ResponseEntity<AuthorDTO.AuthorResponse> uploadAuthorImage(
            @PathVariable Long authorId,
            @RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(authorService.uploadAuthorImage(authorId, file));
    }

    @GetMapping("/popular")
    public ResponseEntity<List<AuthorDTO.AuthorSummary>> getMostPopularAuthors(
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(authorService.getMostPopularAuthors(limit));
    }
}