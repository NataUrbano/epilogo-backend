package com.epilogo.epilogo.controller;

import com.epilogo.epilogo.dto.ReportDTO;
import com.epilogo.epilogo.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Reports", description = "API para reportes de reservas")
public class ReportController {

    private final ReportService reportService;

    @PostMapping("/generate")
    @Operation(summary = "Generar reporte de reservas")
    public ResponseEntity<ByteArrayResource> generateReport(@RequestBody ReportDTO.ReportRequest request) {

        log.info("Generando reporte en formato: {}", request.getFormat());

        try {
            ByteArrayResource resource = reportService.generateReport(request);
            String fileName = reportService.generateFileName(request);

            HttpHeaders headers = new HttpHeaders();

            switch (request.getFormat()) {
                case HTML -> {
                    headers.add(HttpHeaders.CONTENT_TYPE, "text/html; charset=UTF-8");
                    headers.add(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + fileName + "\"");
                }
                case PDF -> {
                    headers.add(HttpHeaders.CONTENT_TYPE, "application/pdf");
                    headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"");
                }
                case CSV -> {
                    headers.add(HttpHeaders.CONTENT_TYPE, "text/csv; charset=UTF-8");
                    headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"");
                }
            }

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(resource);

        } catch (Exception e) {
            log.error("Error generando reporte: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/data")
    @Operation(summary = "Obtener datos del reporte para vista previa")
    public ResponseEntity<List<ReportDTO.ReportData>> getReportData(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String status,
            @RequestParam(required = false, defaultValue = "false") Boolean overdueOnly) {

        ReportDTO.ReportRequest request = ReportDTO.ReportRequest.builder()
                .startDate(startDate)
                .endDate(endDate)
                .status(status != null ? com.epilogo.epilogo.model.Reservation.ReservationStatus.valueOf(status) : null)
                .overdueOnly(overdueOnly)
                .format(ReportDTO.ReportFormat.HTML)
                .build();

        try {
            List<ReportDTO.ReportData> data = reportService.getReportData(request);
            return ResponseEntity.ok(data);
        } catch (Exception e) {
            log.error("Error obteniendo datos del reporte: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/overdue")
    @Operation(summary = "Reporte rápido de reservas vencidas en HTML")
    public ResponseEntity<ByteArrayResource> generateOverdueReport() {
        ReportDTO.ReportRequest request = ReportDTO.ReportRequest.builder()
                .format(ReportDTO.ReportFormat.HTML)
                .overdueOnly(true)
                .build();

        return generateReport(request);
    }

    @GetMapping("/overdue/pdf")
    @Operation(summary = "Reporte rápido de reservas vencidas en PDF")
    public ResponseEntity<ByteArrayResource> generateOverduePdfReport() {
        ReportDTO.ReportRequest request = ReportDTO.ReportRequest.builder()
                .format(ReportDTO.ReportFormat.PDF)
                .overdueOnly(true)
                .build();

        return generateReport(request);
    }

    @GetMapping("/monthly")
    @Operation(summary = "Reporte mensual en CSV")
    public ResponseEntity<ByteArrayResource> generateMonthlyReport() {
        LocalDate now = LocalDate.now();
        LocalDate startOfMonth = now.withDayOfMonth(1);
        LocalDate endOfMonth = now.withDayOfMonth(now.lengthOfMonth());

        ReportDTO.ReportRequest request = ReportDTO.ReportRequest.builder()
                .format(ReportDTO.ReportFormat.CSV)
                .startDate(startOfMonth)
                .endDate(endOfMonth)
                .build();

        return generateReport(request);
    }

    @GetMapping("/monthly/html")
    @Operation(summary = "Reporte mensual en HTML")
    public ResponseEntity<ByteArrayResource> generateMonthlyHtmlReport() {
        LocalDate now = LocalDate.now();
        LocalDate startOfMonth = now.withDayOfMonth(1);
        LocalDate endOfMonth = now.withDayOfMonth(now.lengthOfMonth());

        ReportDTO.ReportRequest request = ReportDTO.ReportRequest.builder()
                .format(ReportDTO.ReportFormat.HTML)
                .startDate(startOfMonth)
                .endDate(endOfMonth)
                .build();

        return generateReport(request);
    }
}