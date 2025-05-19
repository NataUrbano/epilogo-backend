package com.epilogo.epilogo.controller;

import com.epilogo.epilogo.dto.RoleDTO;
import com.epilogo.epilogo.service.RoleService;
import lombok.RequiredArgsConstructor;
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
@RequestMapping("/api/roles")
@RequiredArgsConstructor
@Tag(name = "Roles", description = "API para gestión de roles en el sistema")
@SecurityRequirement(name = "bearerAuth")
public class RoleController {

    private final RoleService roleService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Listar todos los roles", description = "Obtiene la lista completa de roles (solo accesible para administradores)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de roles obtenida correctamente"),
            @ApiResponse(responseCode = "403", description = "No tiene permisos de administrador", content = @Content)
    })
    public ResponseEntity<List<RoleDTO.RoleResponse>> getAllRoles() {
        return ResponseEntity.ok(roleService.getAllRoles());
    }

    @GetMapping("/{roleId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Obtener rol por ID", description = "Obtiene un rol específico por su ID (solo accesible para administradores)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Rol encontrado",
                    content = @Content(schema = @Schema(implementation = RoleDTO.RoleResponse.class))),
            @ApiResponse(responseCode = "403", description = "No tiene permisos de administrador", content = @Content),
            @ApiResponse(responseCode = "404", description = "Rol no encontrado", content = @Content)
    })
    public ResponseEntity<RoleDTO.RoleResponse> getRoleById(
            @Parameter(description = "ID del rol", required = true, example = "1")
            @PathVariable Long roleId) {
        return ResponseEntity.ok(roleService.getRoleById(roleId));
    }

    @GetMapping("/name/{roleName}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Obtener rol por nombre", description = "Obtiene un rol específico por su nombre (solo accesible para administradores)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Rol encontrado",
                    content = @Content(schema = @Schema(implementation = RoleDTO.RoleResponse.class))),
            @ApiResponse(responseCode = "403", description = "No tiene permisos de administrador", content = @Content),
            @ApiResponse(responseCode = "404", description = "Rol no encontrado", content = @Content)
    })
    public ResponseEntity<RoleDTO.RoleResponse> getRoleByName(
            @Parameter(description = "Nombre del rol", required = true, example = "ROLE_ADMIN")
            @PathVariable String roleName) {
        return ResponseEntity.ok(roleService.getRoleByName(roleName));
    }

    @GetMapping("/names")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Listar nombres de roles", description = "Obtiene la lista de todos los nombres de roles disponibles (solo accesible para administradores)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de nombres de roles obtenida correctamente"),
            @ApiResponse(responseCode = "403", description = "No tiene permisos de administrador", content = @Content)
    })
    public ResponseEntity<List<String>> getAllRoleNames() {
        return ResponseEntity.ok(roleService.getAllRoleNames());
    }
}