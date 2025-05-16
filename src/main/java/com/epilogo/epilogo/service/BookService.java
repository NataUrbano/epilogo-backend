package com.epilogo.epilogo.service;

import com.epilogo.epilogo.dto.AuthorDTO;
import com.epilogo.epilogo.dto.BookDTO;
import com.epilogo.epilogo.dto.CategoryDTO;
import com.epilogo.epilogo.exception.ResourceNotFoundException;
import com.epilogo.epilogo.model.Author;
import com.epilogo.epilogo.model.Book;
import com.epilogo.epilogo.model.Category;
import com.epilogo.epilogo.model.Reservation;
import com.epilogo.epilogo.model.S3File;
import com.epilogo.epilogo.model.User;
import com.epilogo.epilogo.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookService {

    private final BookRepository bookRepository;
    private final AuthorRepository authorRepository;
    private final CategoryRepository categoryRepository;
    private final ReservationRepository reservationRepository;
    private final UserRepository userRepository;
    private final S3Service s3Service;
    private final S3FileRepository s3FileRepository;

    /**
     * Get a book by ID with all details
     */
    public BookDTO.BookResponse getBookById(Long bookId) {
        Book book = bookRepository.findByIdWithDetails(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Libro no encontrado con ID: " + bookId));

        // Get current user if authenticated to check if book is reserved by them
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isReservedByCurrentUser = false;

        if (authentication != null && authentication.isAuthenticated() &&
                !authentication.getName().equals("anonymousUser")) {
            String email = authentication.getName();
            Optional<User> currentUser = userRepository.findByEmail(email);

            if (currentUser.isPresent()) {
                Long userId = currentUser.get().getUserId();
                // Check if user has an active reservation for this book
                isReservedByCurrentUser = reservationRepository.findByUserIdAndStatus(userId, Reservation.ReservationStatus.ACTIVE)
                        .stream()
                        .anyMatch(r -> r.getBook().getBookId().equals(bookId));
            }
        }

        // Get active reservations count
        long activeReservations = reservationRepository.countActiveReservationsByBookId(bookId);

        return mapToBookResponse(book, isReservedByCurrentUser, (int) activeReservations);
    }

    /**
     * Search for books with pagination
     */
    public Page<BookDTO.BookResponse> searchBooks(BookDTO.BookSearchRequest request) {
        // Default pagination values
        int page = request.getPage() != null ? request.getPage() : 0;
        int size = request.getSize() != null ? request.getSize() : 10;
        String sortBy = request.getSortBy() != null ? request.getSortBy() : "title";
        String sortDirection = request.getSortDirection() != null ? request.getSortDirection() : "asc";

        // Create sort
        Sort sort = sortDirection.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();

        // Create pageable
        Pageable pageable = PageRequest.of(page, size, sort);

        // Search based on criteria
        Page<Book> booksPage;

        if (request.getQuery() != null && !request.getQuery().isBlank()) {
            booksPage = bookRepository.searchBooks(request.getQuery(), pageable);
        } else if (request.getCategoryId() != null) {
            booksPage = bookRepository.findByCategoryCategoryId(request.getCategoryId(), pageable);
        } else if (request.getAuthorId() != null) {
            booksPage = bookRepository.findByAuthorAuthorId(request.getAuthorId(), pageable);
        } else if (request.getStatus() != null) {
            booksPage = bookRepository.findByBookStatus(request.getStatus(), pageable);
        } else {
            booksPage = bookRepository.findAll(pageable);
        }

        // Check if current user has reserved books
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Long currentUserId = null;

        if (authentication != null && authentication.isAuthenticated() &&
                !authentication.getName().equals("anonymousUser")) {
            String email = authentication.getName();
            Optional<User> currentUser = userRepository.findByEmail(email);

            if (currentUser.isPresent()) {
                currentUserId = currentUser.get().getUserId();
            }
        }

        // Final currentUserId for lambda
        final Long userId = currentUserId;

        // Map to DTO
        return booksPage.map(book -> {
            // Check if book is reserved by current user
            boolean isReservedByCurrentUser = false;
            if (userId != null) {
                isReservedByCurrentUser = reservationRepository.findByUserIdAndStatus(userId, Reservation.ReservationStatus.ACTIVE)
                        .stream()
                        .anyMatch(r -> r.getBook().getBookId().equals(book.getBookId()));
            }

            // Get active reservations count
            long activeReservations = reservationRepository.countActiveReservationsByBookId(book.getBookId());

            return mapToBookResponse(book, isReservedByCurrentUser, (int) activeReservations);
        });
    }

    /**
     * Create a new book
     */
    @Transactional
    public BookDTO.BookResponse createBook(BookDTO.BookCreateRequest request) {
        // Get author
        Author author = authorRepository.findById(request.getAuthorId())
                .orElseThrow(() -> new ResourceNotFoundException("Autor no encontrado con ID: " + request.getAuthorId()));

        // Get category
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada con ID: " + request.getCategoryId()));

        // Create book
        Book book = Book.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .isbn(request.getIsbn())
                .author(author)
                .category(category)
                .totalAmount(request.getTotalAmount())
                .availableAmount(request.getAvailableAmount())
                .publicationYear(request.getPublicationYear())
                .build();

        // Set book status based on available amount
        book.updateBookStatus();

        // Save book
        Book savedBook = bookRepository.save(book);

        return mapToBookResponse(savedBook, false, 0);
    }

    /**
     * Update a book
     */
    @Transactional
    public BookDTO.BookResponse updateBook(Long bookId, BookDTO.BookUpdateRequest request) {
        // Get book
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Libro no encontrado con ID: " + bookId));

        // Update fields if provided
        if (request.getTitle() != null) {
            book.setTitle(request.getTitle());
        }

        if (request.getDescription() != null) {
            book.setDescription(request.getDescription());
        }

        if (request.getIsbn() != null) {
            book.setIsbn(request.getIsbn());
        }

        if (request.getAuthorId() != null) {
            Author author = authorRepository.findById(request.getAuthorId())
                    .orElseThrow(() -> new ResourceNotFoundException("Autor no encontrado con ID: " + request.getAuthorId()));
            book.setAuthor(author);
        }

        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada con ID: " + request.getCategoryId()));
            book.setCategory(category);
        }

        if (request.getTotalAmount() != null) {
            book.setTotalAmount(request.getTotalAmount());
            // Actualizar el estado cada vez que cambie totalAmount
            book.updateBookStatus();
        }

        if (request.getAvailableAmount() != null) {
            // Este setter ya actualiza automáticamente el status
            book.setAvailableAmount(request.getAvailableAmount());
        }

        if (request.getPublicationYear() != null) {
            book.setPublicationYear(request.getPublicationYear());
        }

        // Asegurarse de que el estado esté actualizado antes de guardar
        book.updateBookStatus();

        // Save updated book
        Book updatedBook = bookRepository.save(book);

        // Get active reservations count
        long activeReservations = reservationRepository.countActiveReservationsByBookId(bookId);

        return mapToBookResponse(updatedBook, false, (int) activeReservations);
    }

    /**
     * Delete a book
     */
    @Transactional
    public void deleteBook(Long bookId) {
        // Check if book exists
        if (!bookRepository.existsById(bookId)) {
            throw new ResourceNotFoundException("Libro no encontrado con ID: " + bookId);
        }

        // Check if there are active reservations
        long activeReservations = reservationRepository.countActiveReservationsByBookId(bookId);
        if (activeReservations > 0) {
            throw new IllegalStateException("No se puede eliminar el libro porque tiene reservas activas");
        }

        // Delete book
        bookRepository.deleteById(bookId);
    }

    /**
     * Upload book cover image
     */
    @Transactional
    public BookDTO.BookResponse uploadBookCover(Long bookId, MultipartFile file) {
        // Get book
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Libro no encontrado con ID: " + bookId));

        // Upload file to S3
        S3File s3File = s3Service.uploadFile(file, S3File.EntityType.BOOK, bookId);

        // Update book with image URL
        book.setImageUrl(s3File.getS3Url());
        Book updatedBook = bookRepository.save(book);

        // Get active reservations count
        long activeReservations = reservationRepository.countActiveReservationsByBookId(bookId);

        return mapToBookResponse(updatedBook, false, (int) activeReservations);
    }

    /**
     * Get most popular books
     */
    public List<BookDTO.BookSummary> getMostPopularBooks(int limit) {
        return bookRepository.findMostReservedBooks(limit).stream()
                .map(book -> BookDTO.BookSummary.builder()
                        .bookId(book.getBookId())
                        .title(book.getTitle())
                        .imageUrl(book.getImageUrl())
                        .bookStatus(book.getBookStatus())
                        .authorName(book.getAuthor().getAuthorName())
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * Helper method to map Book entity to BookResponse DTO
     */
    private BookDTO.BookResponse mapToBookResponse(Book book, boolean isReservedByCurrentUser, int activeReservations) {
        Optional<S3File> latestFileOpt = s3FileRepository.findFirstByEntityIdAndEntityTypeOrderByUploadDateDesc(book.getBookId(), S3File.EntityType.BOOK);

        String imageUrl = latestFileOpt.map(S3File::getS3Url).orElse(null);

        return BookDTO.BookResponse.builder()
                .bookId(book.getBookId())
                .title(book.getTitle())
                .description(book.getDescription())
                .isbn(book.getIsbn())
                .author(AuthorDTO.AuthorSummary.builder()
                        .authorId(book.getAuthor().getAuthorId())
                        .authorName(book.getAuthor().getAuthorName())
                        .imageUrl(book.getAuthor().getImageUrl())
                        .build())
                .category(CategoryDTO.CategorySummary.builder()
                        .categoryId(book.getCategory().getCategoryId())
                        .categoryName(book.getCategory().getCategoryName())
                        .imageUrl(book.getCategory().getImageUrl())
                        .build())
                .totalAmount(book.getTotalAmount())
                .availableAmount(book.getAvailableAmount())
                .bookStatus(book.getBookStatus())
                .imageUrl(imageUrl)
                .registerDate(book.getRegisterDate())
                .publicationYear(book.getPublicationYear())
                .activeReservations(activeReservations)
                .isReservedByCurrentUser(isReservedByCurrentUser)
                .build();
    }
}