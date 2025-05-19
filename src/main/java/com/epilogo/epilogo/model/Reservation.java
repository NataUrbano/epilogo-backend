package com.epilogo.epilogo.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import com.fasterxml.jackson.annotation.JsonBackReference;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "reservations")
@EntityListeners(AuditingEntityListener.class)
@Schema(description = "Entidad que representa una reserva o préstamo de un libro")
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reservation_id")
    @Schema(description = "Identificador único de la reserva", example = "1")
    private Long reservationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonBackReference("user-reservations")
    @Schema(description = "Usuario que realiza la reserva", required = true)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    @JsonBackReference("book-reservations")
    @Schema(description = "Libro reservado", required = true)
    private Book book;

    @Column(name = "reservation_date", nullable = false)
    @Schema(description = "Fecha en que se realizó la reserva", example = "2023-05-15", required = true)
    private LocalDate reservationDate;

    @Column(name = "expected_return_date", nullable = false)
    @Schema(description = "Fecha prevista para la devolución del libro", example = "2023-06-15", required = true)
    private LocalDate expectedReturnDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Schema(description = "Estado actual de la reserva", example = "ACTIVE", required = true)
    private ReservationStatus status;

    @Column(name = "actual_return_date")
    @Schema(description = "Fecha real de devolución del libro", example = "2023-06-10")
    private LocalDate actualReturnDate;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    @Schema(description = "Fecha y hora de creación del registro", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    @Schema(description = "Fecha y hora de última actualización del registro", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime updatedAt;

    @Schema(description = "Estados posibles de una reserva")
    public enum ReservationStatus {
        @Schema(description = "Reserva pendiente de confirmar")
        PENDING,
        @Schema(description = "Reserva activa, libro prestado")
        ACTIVE,
        @Schema(description = "Reserva completada, libro devuelto")
        COMPLETED,
        @Schema(description = "Reserva cancelada")
        CANCELLED
    }

    @PrePersist
    public void prePersist() {
        reservationDate = LocalDate.now();
        status = ReservationStatus.PENDING;
    }

    @Transient
    @Schema(description = "Indica si la reserva está vencida (no devuelta después de la fecha esperada)", accessMode = Schema.AccessMode.READ_ONLY)
    public boolean isOverdue() {
        if (status != ReservationStatus.COMPLETED && actualReturnDate == null) {
            return LocalDate.now().isAfter(expectedReturnDate);
        }
        return false;
    }
}