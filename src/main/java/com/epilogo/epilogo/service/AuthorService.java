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

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthorService {

    private final AuthorRepository authorRepository;
    private final S3Service s3Service;
    private final S3FileRepository s3FileRepository;

    /**
     * Get author by ID with all books
     */
    public AuthorDTO.AuthorResponse getAuthorById(Long authorId) {
        Author author = authorRepository.findByIdWithBooks(authorId)
                .orElseThrow(() -> new ResourceNotFoundException("Autor no encontrado con ID: " + authorId));

        return mapToAuthorResponse(author);
    }

    /**
     * Find authors by name with pagination
     */
    public Page<AuthorDTO.AuthorSummary> findAuthorsByName(String name, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("authorName").ascending());

        return authorRepository.findByAuthorNameContainingIgnoreCase(name, pageable)
                .map(this::mapToAuthorSummary);
    }

    /**
     * Get all authors
     */
    public List<AuthorDTO.AuthorSummary> getAllAuthors() {
        return authorRepository.findAll(Sort.by("authorName").ascending()).stream()
                .map(this::mapToAuthorSummary)
                .collect(Collectors.toList());
    }

    /**
     * Create a new author
     */
    @Transactional
    public AuthorDTO.AuthorResponse createAuthor(AuthorDTO.AuthorCreateRequest request) {
        // Check if author name already exists
        if (authorRepository.existsByAuthorNameIgnoreCase(request.getAuthorName())) {
            throw new IllegalStateException("Ya existe un autor con ese nombre");
        }

        // Create author
        Author author = Author.builder()
                .authorName(request.getAuthorName())
                .biography(request.getBiography())
                .birthYear(request.getBirthYear())
                .deathYear(request.getDeathYear())
                .build();

        // Save author
        Author savedAuthor = authorRepository.save(author);

        return mapToAuthorResponse(savedAuthor);
    }

    /**
     * Update an author
     */
    @Transactional
    public AuthorDTO.AuthorResponse updateAuthor(Long authorId, AuthorDTO.AuthorUpdateRequest request) {
        // Get author
        Author author = authorRepository.findById(authorId)
                .orElseThrow(() -> new ResourceNotFoundException("Autor no encontrado con ID: " + authorId));

        // Check if new name exists only if name is changing
        if (request.getAuthorName() != null && !request.getAuthorName().equals(author.getAuthorName())) {
            if (authorRepository.existsByAuthorNameIgnoreCase(request.getAuthorName())) {
                throw new IllegalStateException("Ya existe un autor con ese nombre");
            }
            author.setAuthorName(request.getAuthorName());
        }

        // Update fields if provided
        if (request.getBiography() != null) {
            author.setBiography(request.getBiography());
        }

        if (request.getBirthYear() != null) {
            author.setBirthYear(request.getBirthYear());
        }

        if (request.getDeathYear() != null) {
            author.setDeathYear(request.getDeathYear());
        }

        // Save updated author
        Author updatedAuthor = authorRepository.save(author);

        return mapToAuthorResponse(updatedAuthor);
    }

    /**
     * Delete an author
     */
    @Transactional
    public void deleteAuthor(Long authorId) {
        // Check if author exists
        Author author = authorRepository.findByIdWithBooks(authorId)
                .orElseThrow(() -> new ResourceNotFoundException("Autor no encontrado con ID: " + authorId));

        // Check if author has books
        if (!author.getBooks().isEmpty()) {
            throw new IllegalStateException("No se puede eliminar el autor porque tiene libros asociados");
        }

        // Delete author
        authorRepository.deleteById(authorId);
    }

    /**
     * Upload author image
     */
    @Transactional
    public AuthorDTO.AuthorResponse uploadAuthorImage(Long authorId, MultipartFile file) {
        // Get author
        Author author = authorRepository.findById(authorId)
                .orElseThrow(() -> new ResourceNotFoundException("Autor no encontrado con ID: " + authorId));

        // Upload file to S3
        S3File s3File = s3Service.uploadFile(file, S3File.EntityType.AUTHOR, authorId);

        // Update author with image URL
        author.setImageUrl(s3File.getS3Url());
        Author updatedAuthor = authorRepository.save(author);

        return mapToAuthorResponse(updatedAuthor);
    }

    /**
     * Get most popular authors
     */
    public List<AuthorDTO.AuthorSummary> getMostPopularAuthors(int limit) {
        return authorRepository.findMostPopularAuthors(limit).stream()
                .map(this::mapToAuthorSummary)
                .collect(Collectors.toList());
    }

    /**
     * Helper method to map Author entity to AuthorResponse DTO
     */
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


    /**
     * Helper method to map Author entity to AuthorSummary DTO
     */
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