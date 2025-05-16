package com.epilogo.epilogo.dto;

import com.epilogo.epilogo.model.Reservation.ReservationStatus;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

public class ReservationDTO {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReservationResponse {
        private Long reservationId;
        private UserDTO.UserSummary user;
        private BookDTO.BookSummary book;
        private LocalDate reservationDate;
        private LocalDate expectedReturnDate;
        private ReservationStatus status;
        private LocalDate actualReturnDate;
        private Boolean isOverdue;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReservationCreateRequest {
        @NotNull(message = "El ID del libro es obligatorio")
        private Long bookId;

        @NotNull(message = "La fecha de devolución estimada es obligatoria")
        @Future(message = "La fecha de devolución estimada debe ser en el futuro")
        private LocalDate expectedReturnDate;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReservationUpdateRequest {
        private ReservationStatus status;
        private LocalDate actualReturnDate;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReservationSearchRequest {
        private Long userId;
        private Long bookId;
        private ReservationStatus status;
        private Boolean overdueOnly;
        private LocalDate startDate;
        private LocalDate endDate;
        private Integer page;
        private Integer size;
    }
}