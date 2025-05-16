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

import java.util.List;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;

    @PostMapping
    public ResponseEntity<ReservationDTO.ReservationResponse> createReservation(
            @RequestBody @Valid ReservationDTO.ReservationCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(reservationService.createReservation(request));
    }

    @GetMapping("/{reservationId}")
    public ResponseEntity<ReservationDTO.ReservationResponse> getReservationById(
            @PathVariable Long reservationId) {
        return ResponseEntity.ok(reservationService.getReservationById(reservationId));
    }

    @PutMapping("/{reservationId}")
    public ResponseEntity<ReservationDTO.ReservationResponse> updateReservation(
            @PathVariable Long reservationId,
            @RequestBody @Valid ReservationDTO.ReservationUpdateRequest request) {
        return ResponseEntity.ok(reservationService.updateReservation(reservationId, request));
    }

    @GetMapping
    public ResponseEntity<Page<ReservationDTO.ReservationResponse>> searchReservations(
            @ModelAttribute ReservationDTO.ReservationSearchRequest request) {
        return ResponseEntity.ok(reservationService.searchReservations(request));
    }

    @GetMapping("/overdue")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public ResponseEntity<List<ReservationDTO.ReservationResponse>> getOverdueReservations() {
        return ResponseEntity.ok(reservationService.getOverdueReservations());
    }

    @DeleteMapping("/{reservationId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public ResponseEntity<Void> deleteReservation(@PathVariable Long reservationId) {
        reservationService.deleteReservation(reservationId);
        return ResponseEntity.noContent().build();
    }
}