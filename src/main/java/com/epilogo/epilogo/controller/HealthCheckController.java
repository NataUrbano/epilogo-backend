package com.epilogo.epilogo.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/health")
@Tag(name = "Health Check", description = "API para verificar el estado del servicio")
public class HealthCheckController {

    @GetMapping
    @Operation(summary = "Verificar estado", description = "Devuelve información sobre el estado del servicio")
    @ApiResponse(responseCode = "200", description = "Servicio disponible",
            content = @Content(schema = @Schema(implementation = Map.class)))
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("timestamp", new Date());
        return ResponseEntity.ok(response);
    }
}