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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Tag(name = "Book Service", description = "Servicio para gestionar el catálogo de libros")
public class BookService {

    private final BookRepository bookRepository;
    private final AuthorRepository authorRepository;
    private final CategoryRepository categoryRepository;
    private final ReservationRepository reservationRepository;
    private final UserRepository userRepository;
    private final S3Service s3Service;
    private final S3FileRepository s3FileRepository;

    @Operation(summary = "Obtener libro por ID", description = "Obtiene un libro con todos sus detalles por su ID")
    public BookDTO.BookResponse getBookById(Long bookId) {
        Book book = bookRepository.findByIdWithDetails(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Libro no encontrado con ID: " + bookId));

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isReservedByCurrentUser = false;

        if (authentication != null && authentication.isAuthenticated() &&
                !authentication.getName().equals("anonymousUser")) {
            String email = authentication.getName();
            Optional<User> currentUser = userRepository.findByEmail(email);

            if (currentUser.isPresent()) {
                Long userId = currentUser.get().getUserId();
                isReservedByCurrentUser = reservationRepository.findByUserIdAndStatus(userId, Reservation.ReservationStatus.ACTIVE)
                        .stream()
                        .anyMatch(r -> r.getBook().getBookId().equals(bookId));
            }
        }

        long activeReservations = reservationRepository.countActiveReservationsByBookId(bookId);

        return mapToBookResponse(book, isReservedByCurrentUser, (int) activeReservations);
    }

    @Operation(summary = "Buscar libros", description = "Busca libros con diferentes criterios y paginación")
    public Page<BookDTO.BookResponse> searchBooks(BookDTO.BookSearchRequest request) {
        int page = request.getPage() != null ? request.getPage() : 0;
        int size = request.getSize() != null ? request.getSize() : 10;
        String sortBy = request.getSortBy() != null ? request.getSortBy() : "title";
        String sortDirection = request.getSortDirection() != null ? request.getSortDirection() : "asc";

        Sort sort = sortDirection.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

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

        final Long userId = currentUserId;

        return booksPage.map(book -> {
            boolean isReservedByCurrentUser = false;
            if (userId != null) {
                isReservedByCurrentUser = reservationRepository.findByUserIdAndStatus(userId, Reservation.ReservationStatus.ACTIVE)
                        .stream()
                        .anyMatch(r -> r.getBook().getBookId().equals(book.getBookId()));
            }

            long activeReservations = reservationRepository.countActiveReservationsByBookId(book.getBookId());

            return mapToBookResponse(book, isReservedByCurrentUser, (int) activeReservations);
        });
    }

    @Transactional
    @Operation(summary = "Crear libro", description = "Crea un nuevo libro en el catálogo")
    public BookDTO.BookResponse createBook(BookDTO.BookCreateRequest request) {
        Author author = authorRepository.findById(request.getAuthorId())
                .orElseThrow(() -> new ResourceNotFoundException("Autor no encontrado con ID: " + request.getAuthorId()));

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada con ID: " + request.getCategoryId()));

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

        book.updateBookStatus();

        Book savedBook = bookRepository.save(book);

        return mapToBookResponse(savedBook, false, 0);
    }

    @Transactional
    @Operation(summary = "Actualizar libro", description = "Actualiza los datos de un libro existente")
    public BookDTO.BookResponse updateBook(Long bookId, BookDTO.BookUpdateRequest request) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Libro no encontrado con ID: " + bookId));

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
            book.updateBookStatus();
        }

        if (request.getAvailableAmount() != null) {
            book.setAvailableAmount(request.getAvailableAmount());
        }

        if (request.getPublicationYear() != null) {
            book.setPublicationYear(request.getPublicationYear());
        }

        book.updateBookStatus();

        Book updatedBook = bookRepository.save(book);

        long activeReservations = reservationRepository.countActiveReservationsByBookId(bookId);

        return mapToBookResponse(updatedBook, false, (int) activeReservations);
    }

    @Transactional
    @Operation(summary = "Eliminar libro", description = "Elimina un libro si no tiene reservas activas")
    public void deleteBook(Long bookId) {
        if (!bookRepository.existsById(bookId)) {
            throw new ResourceNotFoundException("Libro no encontrado con ID: " + bookId);
        }

        long activeReservations = reservationRepository.countActiveReservationsByBookId(bookId);
        if (activeReservations > 0) {
            throw new IllegalStateException("No se puede eliminar el libro porque tiene reservas activas");
        }

        bookRepository.deleteById(bookId);
    }

    @Transactional
    @Operation(summary = "Subir portada de libro", description = "Sube una imagen para la portada del libro")
    public BookDTO.BookResponse uploadBookCover(Long bookId, MultipartFile file) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Libro no encontrado con ID: " + bookId));

        S3File s3File = s3Service.uploadFile(file, S3File.EntityType.BOOK, bookId);

        book.setImageUrl(s3File.getS3Url());
        Book updatedBook = bookRepository.save(book);

        long activeReservations = reservationRepository.countActiveReservationsByBookId(bookId);

        return mapToBookResponse(updatedBook, false, (int) activeReservations);
    }

    @Operation(summary = "Obtener libros más populares", description = "Obtiene los libros más reservados recientemente")
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