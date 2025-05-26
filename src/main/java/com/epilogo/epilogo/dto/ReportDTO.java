package com.epilogo.epilogo.dto;

import com.epilogo.epilogo.model.Reservation.ReservationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

@Schema(description = "DTOs para reportes de reservas")
public class ReportDTO {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(name = "ReportRequest", description = "Parámetros para generar reportes")
    public static class ReportRequest {
        @Schema(description = "Formato del reporte", example = "HTML")
        private ReportFormat format;

        @Schema(description = "Fecha de inicio", example = "2023-01-01")
        private LocalDate startDate;

        @Schema(description = "Fecha de fin", example = "2023-12-31")
        private LocalDate endDate;

        @Schema(description = "Estado de reservas", example = "ACTIVE")
        private ReservationStatus status;

        @Schema(description = "Solo reservas vencidas", example = "false")
        private Boolean overdueOnly;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(name = "ReportData", description = "Datos para el reporte")
    public static class ReportData {
        private Long reservationId;
        private String userName;
        private String userEmail;
        private String bookTitle;
        private String bookAuthor;
        private String bookIsbn;
        private LocalDate reservationDate;
        private LocalDate expectedReturnDate;
        private LocalDate actualReturnDate;
        private String status;
        private Boolean isOverdue;
    }

    public enum ReportFormat {
        HTML("text/html", ".html"),
        PDF("application/pdf", ".pdf"),
        CSV("text/csv", ".csv");

        private final String mimeType;
        private final String extension;

        ReportFormat(String mimeType, String extension) {
            this.mimeType = mimeType;
            this.extension = extension;
        }

        public String getMimeType() {
            return mimeType;
        }

        public String getExtension() {
            return extension;
        }
    }
}