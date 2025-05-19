package com.epilogo.epilogo.service;

import com.epilogo.epilogo.dto.AuthorDTO;
import com.epilogo.epilogo.dto.BookDTO;
import com.epilogo.epilogo.exception.ResourceNotFoundException;
import com.epilogo.epilogo.model.Author;
import com.epilogo.epilogo.model.Book;
import com.epilogo.epilogo.model.S3File;
import com.epilogo.epilogo.repository.AuthorRepository;
import com.epilogo.epilogo.repository.S3FileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
@Tag(name = "Author Service", description = "Servicio para gestionar autores de libros")
public class AuthorService {

    private final AuthorRepository authorRepository;
    private final S3Service s3Service;
    private final S3FileRepository s3FileRepository;

    @Operation(summary = "Obtener autor por ID", description = "Obtiene un autor con todos sus libros por su ID")
    public AuthorDTO.AuthorResponse getAuthorById(Long authorId) {
        Author author = authorRepository.findByIdWithBooks(authorId)
                .orElseThrow(() -> new ResourceNotFoundException("Autor no encontrado con ID: " + authorId));

        return mapToAuthorResponse(author);
    }

    @Operation(summary = "Buscar autores por nombre", description = "Busca autores cuyo nombre contenga el texto proporcionado, con paginación")
    public Page<AuthorDTO.AuthorSummary> findAuthorsByName(String name, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("authorName").ascending());

        return authorRepository.findByAuthorNameContainingIgnoreCase(name, pageable)
                .map(this::mapToAuthorSummary);
    }

    @Operation(summary = "Obtener todos los autores", description = "Obtiene una lista resumida de todos los autores ordenados por nombre")
    public List<AuthorDTO.AuthorSummary> getAllAuthors() {
        return authorRepository.findAll(Sort.by("authorName").ascending()).stream()
                .map(this::mapToAuthorSummary)
                .collect(Collectors.toList());
    }

    @Transactional
    @Operation(summary = "Crear autor", description = "Crea un nuevo autor en el sistema")
    public AuthorDTO.AuthorResponse createAuthor(AuthorDTO.AuthorCreateRequest request) {
        if (authorRepository.existsByAuthorNameIgnoreCase(request.getAuthorName())) {
            throw new IllegalStateException("Ya existe un autor con ese nombre");
        }

        Author author = Author.builder()
                .authorName(request.getAuthorName())
                .biography(request.getBiography())
                .birthYear(request.getBirthYear())
                .deathYear(request.getDeathYear())
                .build();

        Author savedAuthor = authorRepository.save(author);

        return mapToAuthorResponse(savedAuthor);
    }

    @Transactional
    @Operation(summary = "Actualizar autor", description = "Actualiza los datos de un autor existente")
    public AuthorDTO.AuthorResponse updateAuthor(Long authorId, AuthorDTO.AuthorUpdateRequest request) {
        Author author = authorRepository.findById(authorId)
                .orElseThrow(() -> new ResourceNotFoundException("Autor no encontrado con ID: " + authorId));

        if (request.getAuthorName() != null && !request.getAuthorName().equals(author.getAuthorName())) {
            if (authorRepository.existsByAuthorNameIgnoreCase(request.getAuthorName())) {
                throw new IllegalStateException("Ya existe un autor con ese nombre");
            }
            author.setAuthorName(request.getAuthorName());
        }

        if (request.getBiography() != null) {
            author.setBiography(request.getBiography());
        }

        if (request.getBirthYear() != null) {
            author.setBirthYear(request.getBirthYear());
        }

        if (request.getDeathYear() != null) {
            author.setDeathYear(request.getDeathYear());
        }

        Author updatedAuthor = authorRepository.save(author);

        return mapToAuthorResponse(updatedAuthor);
    }

    @Transactional
    @Operation(summary = "Eliminar autor", description = "Elimina un autor si no tiene libros asociados")
    public void deleteAuthor(Long authorId) {
        Author author = authorRepository.findByIdWithBooks(authorId)
                .orElseThrow(() -> new ResourceNotFoundException("Autor no encontrado con ID: " + authorId));

        if (!author.getBooks().isEmpty()) {
            throw new IllegalStateException("No se puede eliminar el autor porque tiene libros asociados");
        }

        authorRepository.deleteById(authorId);
    }

    @Transactional
    @Operation(summary = "Subir imagen de autor", description = "Sube una imagen para representar al autor")
    public AuthorDTO.AuthorResponse uploadAuthorImage(Long authorId, MultipartFile file) {
        Author author = authorRepository.findById(authorId)
                .orElseThrow(() -> new ResourceNotFoundException("Autor no encontrado con ID: " + authorId));

        S3File s3File = s3Service.uploadFile(file, S3File.EntityType.AUTHOR, authorId);

        author.setImageUrl(s3File.getS3Url());
        Author updatedAuthor = authorRepository.save(author);

        return mapToAuthorResponse(updatedAuthor);
    }

    @Operation(summary = "Obtener autores más populares", description = "Obtiene los autores más populares según las reservas recientes")
    public List<AuthorDTO.AuthorSummary> getMostPopularAuthors(int limit) {
        return authorRepository.findMostPopularAuthors(limit).stream()
                .map(this::mapToAuthorSummary)
                .collect(Collectors.toList());
    }

    private AuthorDTO.AuthorResponse mapToAuthorResponse(Author author) {
        List<Book> books = author.getBooks() != null ? author.getBooks() : Collections.emptyList();

        Optional<S3File> latestFileOpt = s3FileRepository.findFirstByEntityIdAndEntityTypeOrderByUploadDateDesc(
                author.getAuthorId(), S3File.EntityType.AUTHOR);

        String imageUrl = latestFileOpt.map(S3File::getS3Url).orElse(null);

        return AuthorDTO.AuthorResponse.builder()
                .authorId(author.getAuthorId())
                .authorName(author.getAuthorName())
                .biography(author.getBiography())
                .birthYear(author.getBirthYear())
                .deathYear(author.getDeathYear())
                .imageUrl(imageUrl)
                .books(books.stream()
                        .map(book -> BookDTO.BookSummary.builder()
                                .bookId(book.getBookId())
                                .title(book.getTitle())
                                .imageUrl(book.getImageUrl())
                                .bookStatus(book.getBookStatus())
                                .authorName(author.getAuthorName())
                                .build())
                        .collect(Collectors.toList()))
                .totalBooks(books.size())
                .build();
    }

    private AuthorDTO.AuthorSummary mapToAuthorSummary(Author author) {
        Optional<S3File> latestFileOpt = s3FileRepository.findFirstByEntityIdAndEntityTypeOrderByUploadDateDesc(author.getAuthorId(), S3File.EntityType.AUTHOR);

        String imageUrl = latestFileOpt.map(S3File::getS3Url).orElse(null);

        return AuthorDTO.AuthorSummary.builder()
                .authorId(author.getAuthorId())
                .authorName(author.getAuthorName())
                .imageUrl(imageUrl)
                .build();
    }
}