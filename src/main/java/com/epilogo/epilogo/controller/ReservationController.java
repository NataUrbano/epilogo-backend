package com.epilogo.epilogo.controller;

import com.epilogo.epilogo.dto.ReservationDTO;
import com.epilogo.epilogo.service.ReservationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import java.util.List;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
@Tag(name = "Reservas", description = "API para gestión de reservas y préstamos de libros")
@SecurityRequirement(name = "bearerAuth")
public class ReservationController {

    private final ReservationService reservationService;

    @PostMapping
    @Operation(summary = "Crear reserva", description = "Crea una nueva reserva de libro para el usuario autenticado")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Reserva creada correctamente",
                    content = @Content(schema = @Schema(implementation = ReservationDTO.ReservationResponse.class))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos o no hay ejemplares disponibles", content = @Content),
            @ApiResponse(responseCode = "404", description = "Libro no encontrado", content = @Content)
    })
    public ResponseEntity<ReservationDTO.ReservationResponse> createReservation(
            @Parameter(description = "Datos de la reserva a crear", required = true)
            @RequestBody @Valid ReservationDTO.ReservationCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(reservationService.createReservation(request));
    }

    @GetMapping("/{reservationId}")
    @Operation(summary = "Obtener reserva por ID", description = "Devuelve información completa de una reserva")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Reserva encontrada",
                    content = @Content(schema = @Schema(implementation = ReservationDTO.ReservationResponse.class))),
            @ApiResponse(responseCode = "403", description = "No tiene permisos para ver esta reserva", content = @Content),
            @ApiResponse(responseCode = "404", description = "Reserva no encontrada", content = @Content)
    })
    public ResponseEntity<ReservationDTO.ReservationResponse> getReservationById(
            @Parameter(description = "ID de la reserva", required = true, example = "1")
            @PathVariable Long reservationId) {
        return ResponseEntity.ok(reservationService.getReservationById(reservationId));
    }

    @PutMapping("/{reservationId}")
    @Operation(summary = "Actualizar reserva", description = "Actualiza el estado de una reserva existente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Reserva actualizada correctamente",
                    content = @Content(schema = @Schema(implementation = ReservationDTO.ReservationResponse.class))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos", content = @Content),
            @ApiResponse(responseCode = "403", description = "No tiene permisos para actualizar esta reserva", content = @Content),
            @ApiResponse(responseCode = "404", description = "Reserva no encontrada", content = @Content)
    })
    public ResponseEntity<ReservationDTO.ReservationResponse> updateReservation(
            @Parameter(description = "ID de la reserva a actualizar", required = true, example = "1")
            @PathVariable Long reservationId,
            @Parameter(description = "Datos actualizados de la reserva", required = true)
            @RequestBody @Valid ReservationDTO.ReservationUpdateRequest request) {
        return ResponseEntity.ok(reservationService.updateReservation(reservationId, request));
    }

    @GetMapping
    @Operation(summary = "Buscar reservas", description = "Busca reservas según diferentes criterios con paginación")
    @ApiResponse(responseCode = "200", description = "Búsqueda realizada correctamente")
    public ResponseEntity<Page<ReservationDTO.ReservationResponse>> searchReservations(
            @Parameter(description = "Criterios de búsqueda")
            @ModelAttribute ReservationDTO.ReservationSearchRequest request) {
        return ResponseEntity.ok(reservationService.searchReservations(request));
    }

    @GetMapping("/overdue")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    @Operation(summary = "Obtener reservas vencidas", description = "Obtiene todas las reservas vencidas (solo para administradores y bibliotecarios)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de reservas vencidas obtenida correctamente"),
            @ApiResponse(responseCode = "403", description = "No tiene permisos suficientes", content = @Content)
    })
    public ResponseEntity<List<ReservationDTO.ReservationResponse>> getOverdueReservations() {
        return ResponseEntity.ok(reservationService.getOverdueReservations());
    }

    @DeleteMapping("/{reservationId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    @Operation(summary = "Eliminar reserva", description = "Elimina permanentemente una reserva (solo para administradores y bibliotecarios)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Reserva eliminada correctamente"),
            @ApiResponse(responseCode = "403", description = "No tiene permisos suficientes", content = @Content),
            @ApiResponse(responseCode = "404", description = "Reserva no encontrada", content = @Content)
    })
    public ResponseEntity<Void> deleteReservation(
            @Parameter(description = "ID de la reserva a eliminar", required = true, example = "1")
            @PathVariable Long reservationId) {
        reservationService.deleteReservation(reservationId);
        return ResponseEntity.noContent().build();
    }
}