package com.epilogo.epilogo.service;

import com.epilogo.epilogo.dto.BookDTO;
import com.epilogo.epilogo.dto.ReservationDTO;
import com.epilogo.epilogo.dto.UserDTO;
import com.epilogo.epilogo.exception.ResourceNotFoundException;
import com.epilogo.epilogo.model.Book;
import com.epilogo.epilogo.model.Reservation;
import com.epilogo.epilogo.model.User;
import com.epilogo.epilogo.repository.BookRepository;
import com.epilogo.epilogo.repository.ReservationRepository;
import com.epilogo.epilogo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final UserRepository userRepository;
    private final BookRepository bookRepository;

    /**
     * Create a new reservation
     */
    @Transactional
    public ReservationDTO.ReservationResponse createReservation(ReservationDTO.ReservationCreateRequest request) {
        // Get authenticated user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        // Get book
        Book book = bookRepository.findById(request.getBookId())
                .orElseThrow(() -> new ResourceNotFoundException("Libro no encontrado con ID: " + request.getBookId()));

        // Check if book is available
        if (book.getAvailableAmount() <= 0) {
            throw new IllegalStateException("No hay copias disponibles de este libro");
        }

        // Create reservation
        Reservation reservation = Reservation.builder()
                .user(user)
                .book(book)
                .reservationDate(LocalDate.now())
                .expectedReturnDate(request.getExpectedReturnDate())
                .status(Reservation.ReservationStatus.PENDING)
                .build();

        // Save reservation
        Reservation savedReservation = reservationRepository.save(reservation);

        // TRIGGER LOGIC: Si la nueva reserva es ACTIVE (aunque normalmente será PENDING)
        if (savedReservation.getStatus() == Reservation.ReservationStatus.ACTIVE) {
            decreaseBookAvailability(book);
        }

        return mapToReservationResponse(savedReservation);
    }

    /**
     * Get reservation by ID
     */
    public ReservationDTO.ReservationResponse getReservationById(Long reservationId) {
        Reservation reservation = reservationRepository.findByIdWithDetails(reservationId);
        if (reservation == null) {
            throw new ResourceNotFoundException("Reserva no encontrada con ID: " + reservationId);
        }

        // Check if user is authorized to see this reservation
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        // Only allow users to see their own reservations unless they're an admin/librarian
        boolean isAdminOrLibrarian = currentUser.getRoles().stream()
                .anyMatch(role -> role.getRoleName().name().equals("ROLE_ADMIN") ||
                        role.getRoleName().name().equals("ROLE_LIBRARIAN"));

        if (!currentUser.getUserId().equals(reservation.getUser().getUserId()) && !isAdminOrLibrarian) {
            throw new AccessDeniedException("No está autorizado para ver esta reserva");
        }

        return mapToReservationResponse(reservation);
    }

    /**
     * Update reservation status
     */
    @Transactional
    public ReservationDTO.ReservationResponse updateReservation(Long reservationId, ReservationDTO.ReservationUpdateRequest request) {
        // Check if user is authorized
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        // Get reservation
        Reservation reservation = reservationRepository.findByIdWithDetails(reservationId);
        if (reservation == null) {
            throw new ResourceNotFoundException("Reserva no encontrada con ID: " + reservationId);
        }

        // Only allow users to update their own reservations or admin/librarian
        boolean isAdminOrLibrarian = currentUser.getRoles().stream()
                .anyMatch(role -> role.getRoleName().name().equals("ROLE_ADMIN") ||
                        role.getRoleName().name().equals("ROLE_LIBRARIAN"));

        if (!currentUser.getUserId().equals(reservation.getUser().getUserId()) && !isAdminOrLibrarian) {
            throw new AccessDeniedException("No está autorizado para actualizar esta reserva");
        }

        // Guardar estado anterior para la lógica del trigger
        Reservation.ReservationStatus oldStatus = reservation.getStatus();

        // Update status if provided
        if (request.getStatus() != null) {
            // Check if status change is valid
            validateStatusChange(oldStatus, request.getStatus(), isAdminOrLibrarian);

            // TRIGGER LOGIC: Actualizar available_amount basándose en cambios de estado
            handleReservationStatusChange(reservation, oldStatus, request.getStatus());

            reservation.setStatus(request.getStatus());
        }

        // Update actual return date if provided
        if (request.getActualReturnDate() != null) {
            reservation.setActualReturnDate(request.getActualReturnDate());
        }

        // Save updated reservation
        Reservation updatedReservation = reservationRepository.save(reservation);

        return mapToReservationResponse(updatedReservation);
    }

    /**
     * Search for reservations with pagination
     */
    public Page<ReservationDTO.ReservationResponse> searchReservations(ReservationDTO.ReservationSearchRequest request) {
        // Default pagination values
        int page = request.getPage() != null ? request.getPage() : 0;
        int size = request.getSize() != null ? request.getSize() : 10;

        // Create pageable with sort by reservation date desc
        Pageable pageable = PageRequest.of(page, size, Sort.by("reservationDate").descending());

        // Get authenticated user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        // Regular users can only see their own reservations
        boolean isAdminOrLibrarian = currentUser.getRoles().stream()
                .anyMatch(role -> role.getRoleName().name().equals("ROLE_ADMIN") ||
                        role.getRoleName().name().equals("ROLE_LIBRARIAN"));

        Page<Reservation> reservationsPage;

        if (!isAdminOrLibrarian) {
            // Regular user can only see their own reservations
            reservationsPage = reservationRepository.findByUserUserId(currentUser.getUserId(), pageable);
        } else {
            // Admin/Librarian can see reservations based on search criteria
            if (request.getUserId() != null) {
                reservationsPage = reservationRepository.findByUserUserId(request.getUserId(), pageable);
            } else if (request.getBookId() != null) {
                reservationsPage = reservationRepository.findByBookBookId(request.getBookId(), pageable);
            } else {
                // If no specific criteria, get all reservations
                reservationsPage = reservationRepository.findAll(pageable);
            }
        }

        // Map to DTOs
        return reservationsPage.map(this::mapToReservationResponse);
    }

    /**
     * Get overdue reservations (for admin/librarian)
     */
    public List<ReservationDTO.ReservationResponse> getOverdueReservations() {
        LocalDate today = LocalDate.now();
        return reservationRepository.findOverdueReservations(today).stream()
                .map(this::mapToReservationResponse)
                .collect(Collectors.toList());
    }

    /**
     * Delete a reservation (optional method for handling DELETE trigger logic)
     */
    @Transactional
    public void deleteReservation(Long reservationId) {
        // Get reservation
        Reservation reservation = reservationRepository.findByIdWithDetails(reservationId);
        if (reservation == null) {
            throw new ResourceNotFoundException("Reserva no encontrada con ID: " + reservationId);
        }

        // TRIGGER LOGIC: Si se elimina una reserva con estado ACTIVE, aumentar disponibilidad
        if (reservation.getStatus() == Reservation.ReservationStatus.ACTIVE) {
            increaseBookAvailability(reservation.getBook());
        }

        // Delete reservation
        reservationRepository.deleteById(reservationId);
    }

    /**
     * Lógica del primer trigger: manejar cambios de estado en reservas
     */
    private void handleReservationStatusChange(Reservation reservation,
                                               Reservation.ReservationStatus oldStatus,
                                               Reservation.ReservationStatus newStatus) {
        Book book = reservation.getBook();

        // Si la reserva cambia de otro estado a ACTIVE
        if (oldStatus != Reservation.ReservationStatus.ACTIVE && newStatus == Reservation.ReservationStatus.ACTIVE) {
            decreaseBookAvailability(book);
        }
        // Si la reserva cambia de ACTIVE a COMPLETED o CANCELLED
        else if (oldStatus == Reservation.ReservationStatus.ACTIVE &&
                (newStatus == Reservation.ReservationStatus.COMPLETED || newStatus == Reservation.ReservationStatus.CANCELLED)) {
            increaseBookAvailability(book);
        }
    }

    /**
     * Disminuir disponibilidad del libro
     */
    private void decreaseBookAvailability(Book book) {
        if (book.getAvailableAmount() > 0) {
            book.setAvailableAmount(book.getAvailableAmount() - 1);
            // El método setAvailableAmount automáticamente actualiza el status
            bookRepository.save(book);
        }
    }

    /**
     * Aumentar disponibilidad del libro
     */
    private void increaseBookAvailability(Book book) {
        if (book.getAvailableAmount() < book.getTotalAmount()) {
            book.setAvailableAmount(book.getAvailableAmount() + 1);
            // El método setAvailableAmount automáticamente actualiza el status
            bookRepository.save(book);
        }
    }

    /**
     * Helper method to validate status change
     */
    private void validateStatusChange(Reservation.ReservationStatus currentStatus,
                                      Reservation.ReservationStatus newStatus,
                                      boolean isAdminOrLibrarian) {
        // Only admin/librarian can change to ACTIVE
        if (newStatus == Reservation.ReservationStatus.ACTIVE && !isAdminOrLibrarian) {
            throw new AccessDeniedException("Solo un administrador o bibliotecario puede activar una reserva");
        }

        // Only admin/librarian can mark as COMPLETED
        if (newStatus == Reservation.ReservationStatus.COMPLETED && !isAdminOrLibrarian) {
            throw new AccessDeniedException("Solo un administrador o bibliotecario puede completar una reserva");
        }

        // Users can only cancel their own reservations if they are PENDING
        if (newStatus == Reservation.ReservationStatus.CANCELLED) {
            if (currentStatus != Reservation.ReservationStatus.PENDING && !isAdminOrLibrarian) {
                throw new IllegalStateException("Solo se pueden cancelar reservas en estado PENDIENTE");
            }
        }

        // Cannot change if already COMPLETED or CANCELLED
        if ((currentStatus == Reservation.ReservationStatus.COMPLETED ||
                currentStatus == Reservation.ReservationStatus.CANCELLED) &&
                !isAdminOrLibrarian) {
            throw new IllegalStateException("No se puede modificar una reserva que ya está completada o cancelada");
        }
    }

    /**
     * Helper method to map Reservation entity to ReservationResponse DTO
     */
    private ReservationDTO.ReservationResponse mapToReservationResponse(Reservation reservation) {
        return ReservationDTO.ReservationResponse.builder()
                .reservationId(reservation.getReservationId())
                .user(UserDTO.UserSummary.builder()
                        .userId(reservation.getUser().getUserId())
                        .userName(reservation.getUser().getUserName())
                        .imageUrl(reservation.getUser().getImageUrl())
                        .build())
                .book(BookDTO.BookSummary.builder()
                        .bookId(reservation.getBook().getBookId())
                        .title(reservation.getBook().getTitle())
                        .imageUrl(reservation.getBook().getImageUrl())
                        .bookStatus(reservation.getBook().getBookStatus())
                        .authorName(reservation.getBook().getAuthor().getAuthorName())
                        .build())
                .reservationDate(reservation.getReservationDate())
                .expectedReturnDate(reservation.getExpectedReturnDate())
                .status(reservation.getStatus())
                .actualReturnDate(reservation.getActualReturnDate())
                .isOverdue(reservation.isOverdue())
                .build();
    }
}