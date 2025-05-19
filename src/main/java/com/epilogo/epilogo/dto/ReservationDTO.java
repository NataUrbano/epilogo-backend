package com.epilogo.epilogo.dto;

import com.epilogo.epilogo.model.Reservation.ReservationStatus;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

@Schema(description = "DTOs relacionados con la gestión de reservas de libros")
public class ReservationDTO {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(name = "ReservationResponse", description = "Respuesta con los datos de una reserva")
    public static class ReservationResponse {
        @Schema(description = "Identificador único de la reserva", example = "1")
        private Long reservationId;

        @Schema(description = "Datos resumidos del usuario que realizó la reserva")
        private UserDTO.UserSummary user;

        @Schema(description = "Datos resumidos del libro reservado")
        private BookDTO.BookSummary book;

        @Schema(description = "Fecha en que se realizó la reserva", example = "2023-05-15")
        private LocalDate reservationDate;

        @Schema(description = "Fecha prevista para la devolución", example = "2023-06-15")
        private LocalDate expectedReturnDate;

        @Schema(description = "Estado actual de la reserva", example = "ACTIVE")
        private ReservationStatus status;

        @Schema(description = "Fecha real de devolución (si ya fue devuelto)", example = "2023-06-10")
        private LocalDate actualReturnDate;

        @Schema(description = "Indica si la reserva está vencida (no devuelta después de la fecha esperada)", example = "false")
        private Boolean isOverdue;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(name = "ReservationCreateRequest", description = "Datos requeridos para crear una nueva reserva")
    public static class ReservationCreateRequest {
        @NotNull(message = "El ID del libro es obligatorio")
        @Schema(description = "ID del libro a reservar", example = "1", required = true)
        private Long bookId;

        @NotNull(message = "La fecha de devolución estimada es obligatoria")
        @Future(message = "La fecha de devolución estimada debe ser en el futuro")
        @Schema(description = "Fecha estimada de devolución", example = "2023-06-15", required = true)
        private LocalDate expectedReturnDate;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(name = "ReservationUpdateRequest", description = "Datos para actualizar una reserva existente")
    public static class ReservationUpdateRequest {
        @Schema(description = "Nuevo estado de la reserva", example = "COMPLETED")
        private ReservationStatus status;

        @Schema(description = "Fecha real de devolución del libro", example = "2023-06-10")
        private LocalDate actualReturnDate;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(name = "ReservationSearchRequest", description = "Criterios de búsqueda para reservas")
    public static class ReservationSearchRequest {
        @Schema(description = "ID del usuario para filtrar sus reservas", example = "1")
        private Long userId;

        @Schema(description = "ID del libro para filtrar sus reservas", example = "2")
        private Long bookId;

        @Schema(description = "Estado de las reservas a buscar", example = "ACTIVE")
        private ReservationStatus status;

        @Schema(description = "Si es true, solo muestra reservas vencidas", example = "false")
        private Boolean overdueOnly;

        @Schema(description = "Fecha de inicio para filtrar reservas", example = "2023-01-01")
        private LocalDate startDate;

        @Schema(description = "Fecha de fin para filtrar reservas", example = "2023-12-31")
        private LocalDate endDate;

        @Schema(description = "Número de página (para paginación)", example = "0")
        private Integer page;

        @Schema(description = "Tamaño de página (para paginación)", example = "10")
        private Integer size;
    }
}