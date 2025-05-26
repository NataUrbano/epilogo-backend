package com.epilogo.epilogo.service;

import com.epilogo.epilogo.dto.ReportDTO;
import com.epilogo.epilogo.model.Reservation;
import com.epilogo.epilogo.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReportService {

    private final ReservationRepository reservationRepository;

    public ByteArrayResource generateReport(ReportDTO.ReportRequest request) {
        try {
            List<ReportDTO.ReportData> reportData = getReportData(request);

            byte[] reportBytes = switch (request.getFormat()) {
                case HTML -> generateHtmlReport(reportData).getBytes("UTF-8");
                case PDF -> generateJasperPdfReport(reportData, request);
                case CSV -> generateCsvReport(reportData).getBytes("UTF-8");
            };

            return new ByteArrayResource(reportBytes);

        } catch (Exception e) {
            log.error("Error generando reporte: {}", e.getMessage(), e);
            throw new RuntimeException("Error al generar el reporte: " + e.getMessage());
        }
    }

    public List<ReportDTO.ReportData> getReportData(ReportDTO.ReportRequest request) {
        List<Reservation> reservations = getFilteredReservations(request);
        return convertToReportData(reservations);
    }

    private List<Reservation> getFilteredReservations(ReportDTO.ReportRequest request) {
        List<Reservation> allReservations = reservationRepository.findAll();

        return allReservations.stream()
                .filter(reservation -> matchesFilters(reservation, request))
                .collect(Collectors.toList());
    }

    private boolean matchesFilters(Reservation reservation, ReportDTO.ReportRequest request) {
        if (request.getStartDate() != null &&
                reservation.getReservationDate().isBefore(request.getStartDate())) {
            return false;
        }

        if (request.getEndDate() != null &&
                reservation.getReservationDate().isAfter(request.getEndDate())) {
            return false;
        }

        if (request.getStatus() != null &&
                !reservation.getStatus().equals(request.getStatus())) {
            return false;
        }

        if (request.getOverdueOnly() != null && request.getOverdueOnly()) {
            if (reservation.getStatus() == Reservation.ReservationStatus.COMPLETED ||
                    reservation.getActualReturnDate() != null ||
                    !LocalDate.now().isAfter(reservation.getExpectedReturnDate())) {
                return false;
            }
        }

        return true;
    }

    private List<ReportDTO.ReportData> convertToReportData(List<Reservation> reservations) {
        return reservations.stream()
                .map(this::convertToReportData)
                .collect(Collectors.toList());
    }

    private ReportDTO.ReportData convertToReportData(Reservation reservation) {
        return ReportDTO.ReportData.builder()
                .reservationId(reservation.getReservationId())
                .userName(reservation.getUser() != null ? reservation.getUser().getUserName() : "N/A")
                .userEmail(reservation.getUser() != null ? reservation.getUser().getEmail() : "N/A")
                .bookTitle(reservation.getBook() != null ? reservation.getBook().getTitle() : "N/A")
                .bookAuthor(reservation.getBook() != null && reservation.getBook().getAuthor() != null ?
                        reservation.getBook().getAuthor().getAuthorName() : "N/A")
                .bookIsbn(reservation.getBook() != null ? reservation.getBook().getIsbn() : "N/A")
                .reservationDate(reservation.getReservationDate())
                .expectedReturnDate(reservation.getExpectedReturnDate())
                .actualReturnDate(reservation.getActualReturnDate())
                .status(reservation.getStatus().name())
                .isOverdue(reservation.isOverdue())
                .build();
    }

    private byte[] generateJasperPdfReport(List<ReportDTO.ReportData> data, ReportDTO.ReportRequest request) throws JRException {
        try {
            ClassPathResource resource = new ClassPathResource("reports/reservations_report.jrxml");

            if (!resource.exists()) {
                log.warn("Archivo JasperReports no encontrado: {}", resource.getPath());
                throw new JRException("Template de reporte no encontrado: " + resource.getPath());
            }

            InputStream reportStream = resource.getInputStream();
            JasperReport jasperReport = JasperCompileManager.compileReport(reportStream);

            Map<String, Object> parameters = new HashMap<>();
            parameters.put("REPORT_TITLE", "Reporte de Reservas");
            parameters.put("GENERATED_DATE", LocalDate.now());
            parameters.put("TOTAL_RECORDS", data.size());

            JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(data);

            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, dataSource);

            return JasperExportManager.exportReportToPdf(jasperPrint);

        } catch (Exception e) {
            log.error("Error generando PDF con JasperReports: {}", e.getMessage(), e);
            throw new JRException("Error al generar PDF: " + e.getMessage(), e);
        }
    }

    private String generateHtmlReport(List<ReportDTO.ReportData> data) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        StringBuilder html = new StringBuilder();

        html.append("<!DOCTYPE html>\n<html lang='es'>\n<head>\n");
        html.append("<meta charset='UTF-8'>\n");
        html.append("<meta name='viewport' content='width=device-width, initial-scale=1.0'>\n");
        html.append("<title>Reporte de Reservas - Sistema Epilogo</title>\n");
        html.append("<style>\n");
        html.append("* { margin: 0; padding: 0; box-sizing: border-box; }\n");
        html.append("body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; margin: 20px; background: #fff; color: #333; }\n");
        html.append(".header { text-align: center; margin-bottom: 30px; padding: 20px; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; border-radius: 10px; }\n");
        html.append(".header h1 { font-size: 28px; margin-bottom: 10px; }\n");
        html.append(".header p { font-size: 16px; opacity: 0.9; }\n");
        html.append(".info { display: flex; justify-content: space-between; margin-bottom: 20px; padding: 15px; background: #f8f9fa; border-radius: 5px; }\n");
        html.append(".info div { font-weight: bold; }\n");
        html.append("table { width: 100%; border-collapse: collapse; margin-top: 20px; box-shadow: 0 2px 8px rgba(0,0,0,0.1); }\n");
        html.append("th { background: #4a90e2; color: white; padding: 12px 8px; text-align: left; font-weight: 600; }\n");
        html.append("td { padding: 10px 8px; border-bottom: 1px solid #e0e0e0; }\n");
        html.append("tr:nth-child(even) { background-color: #f8f9fa; }\n");
        html.append("tr:hover { background-color: #e3f2fd; }\n");
        html.append(".overdue { color: #d32f2f; font-weight: bold; }\n");
        html.append(".status-active { color: #1976d2; font-weight: 500; }\n");
        html.append(".status-completed { color: #388e3c; font-weight: 500; }\n");
        html.append(".status-pending { color: #f57c00; font-weight: 500; }\n");
        html.append(".status-cancelled { color: #757575; font-weight: 500; }\n");
        html.append(".footer { text-align: center; margin-top: 30px; padding: 15px; color: #666; border-top: 2px solid #e0e0e0; }\n");
        html.append("</style>\n");
        html.append("</head>\n<body>\n");

        html.append("<div class='header'>\n");
        html.append("<h1>📚 SISTEMA EPILOGO</h1>\n");
        html.append("<p>Reporte de Reservas</p>\n");
        html.append("</div>\n");

        html.append("<div class='info'>\n");
        html.append("<div>📅 Fecha: ").append(LocalDate.now().format(formatter)).append("</div>\n");
        html.append("<div>📊 Total: ").append(data.size()).append(" registros</div>\n");
        html.append("</div>\n");

        html.append("<table>\n");
        html.append("<thead>\n<tr>\n");
        html.append("<th>ID</th><th>Usuario</th><th>Email</th><th>Libro</th><th>Autor</th>\n");
        html.append("<th>F. Reserva</th><th>F. Esperada</th><th>F. Real</th><th>Estado</th>\n");
        html.append("</tr>\n</thead>\n<tbody>\n");

        for (ReportDTO.ReportData item : data) {
            html.append("<tr>\n");
            html.append("<td>").append(item.getReservationId()).append("</td>\n");
            html.append("<td>").append(item.getUserName()).append("</td>\n");
            html.append("<td>").append(item.getUserEmail()).append("</td>\n");
            html.append("<td>").append(item.getBookTitle()).append("</td>\n");
            html.append("<td>").append(item.getBookAuthor()).append("</td>\n");
            html.append("<td>").append(item.getReservationDate().format(formatter)).append("</td>\n");
            html.append("<td>").append(item.getExpectedReturnDate().format(formatter)).append("</td>\n");
            html.append("<td>").append(item.getActualReturnDate() != null ?
                    item.getActualReturnDate().format(formatter) : "-").append("</td>\n");

            String statusClass = "";
            String statusText = getStatusText(item.getStatus());

            if (item.getIsOverdue()) {
                statusClass = "overdue";
                statusText = statusText + " (VENCIDA)";
            } else {
                statusClass = "status-" + item.getStatus().toLowerCase();
            }

            html.append("<td class='").append(statusClass).append("'>").append(statusText).append("</td>\n");
            html.append("</tr>\n");
        }

        html.append("</tbody>\n</table>\n");

        html.append("<div class='footer'>\n");
        html.append("<p>Sistema Epilogo - Gestión de Biblioteca</p>\n");
        html.append("<p><em>💡 Para imprimir como PDF: Archivo → Imprimir → Guardar como PDF</em></p>\n");
        html.append("</div>\n");

        html.append("</body>\n</html>");

        return html.toString();
    }

    private String generateCsvReport(List<ReportDTO.ReportData> data) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        StringBuilder csv = new StringBuilder();

        csv.append("\uFEFF");
        csv.append("ID,Usuario,Email,Libro,Autor,F_Reserva,F_Esperada,F_Real,Estado,Vencida\n");

        for (ReportDTO.ReportData item : data) {
            csv.append(item.getReservationId()).append(",");
            csv.append("\"").append(item.getUserName()).append("\",");
            csv.append("\"").append(item.getUserEmail()).append("\",");
            csv.append("\"").append(item.getBookTitle()).append("\",");
            csv.append("\"").append(item.getBookAuthor()).append("\",");
            csv.append(item.getReservationDate().format(formatter)).append(",");
            csv.append(item.getExpectedReturnDate().format(formatter)).append(",");
            csv.append(item.getActualReturnDate() != null ?
                    item.getActualReturnDate().format(formatter) : "").append(",");
            csv.append("\"").append(getStatusText(item.getStatus())).append("\",");
            csv.append(item.getIsOverdue() ? "SI" : "NO").append("\n");
        }

        return csv.toString();
    }

    private String getStatusText(String status) {
        return switch (status) {
            case "PENDING" -> "Pendiente";
            case "ACTIVE" -> "Activa";
            case "COMPLETED" -> "Completada";
            case "CANCELLED" -> "Cancelada";
            default -> status;
        };
    }

    public String generateFileName(ReportDTO.ReportRequest request) {
        String timestamp = LocalDate.now().toString();
        String extension = request.getFormat().getExtension();
        return String.format("reporte_reservas_%s%s", timestamp, extension);
    }
}