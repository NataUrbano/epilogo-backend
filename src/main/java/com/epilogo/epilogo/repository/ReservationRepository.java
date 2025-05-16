package com.epilogo.epilogo.repository;

import com.epilogo.epilogo.model.Reservation;
import com.epilogo.epilogo.model.Reservation.ReservationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    Page<Reservation> findByUserUserId(Long userId, Pageable pageable);

    Page<Reservation> findByBookBookId(Long bookId, Pageable pageable);

    List<Reservation> findByStatus(ReservationStatus status);

    @Query("SELECT r FROM Reservation r WHERE r.status = 'ACTIVE' AND r.expectedReturnDate < :today")
    List<Reservation> findOverdueReservations(@Param("today") LocalDate today);

    @Query("SELECT r FROM Reservation r WHERE r.user.userId = :userId AND r.status = :status")
    List<Reservation> findByUserIdAndStatus(@Param("userId") Long userId, @Param("status") ReservationStatus status);

    @Query("SELECT COUNT(r) FROM Reservation r WHERE r.book.bookId = :bookId AND r.status IN ('PENDING', 'ACTIVE')")
    long countActiveReservationsByBookId(@Param("bookId") Long bookId);

    @Query("SELECT r FROM Reservation r JOIN FETCH r.user JOIN FETCH r.book WHERE r.reservationId = :id")
    Reservation findByIdWithDetails(@Param("id") Long id);

    @Query(value = "SELECT DATE(r.reservation_date) as date, COUNT(*) as count FROM reservations r " +
            "WHERE r.reservation_date BETWEEN :startDate AND :endDate " +
            "GROUP BY DATE(r.reservation_date) " +
            "ORDER BY date", nativeQuery = true)
    List<Object[]> getReservationsCountByDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}