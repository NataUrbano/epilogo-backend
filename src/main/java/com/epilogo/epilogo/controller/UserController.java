package com.epilogo.epilogo.controller;

import com.epilogo.epilogo.dto.UserDTO;
import com.epilogo.epilogo.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "API para gestión de usuarios")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    @Operation(summary = "Obtener usuario actual", description = "Devuelve la información del usuario autenticado")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Información del usuario obtenida correctamente",
                    content = @Content(schema = @Schema(implementation = UserDTO.UserResponse.class))),
            @ApiResponse(responseCode = "401", description = "No autorizado", content = @Content)
    })
    public ResponseEntity<UserDTO.UserResponse> getCurrentUser() {
        return ResponseEntity.ok(userService.getCurrentUser());
    }

    @GetMapping("/{userId}")
    @Operation(summary = "Obtener usuario por ID", description = "Devuelve la información de un usuario según su ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuario encontrado",
                    content = @Content(schema = @Schema(implementation = UserDTO.UserResponse.class))),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado", content = @Content)
    })
    public ResponseEntity<UserDTO.UserResponse> getUserById(
            @Parameter(description = "ID del usuario", required = true)
            @PathVariable Long userId) {
        return ResponseEntity.ok(userService.getUserById(userId));
    }

    @PutMapping("/{userId}")
    @Operation(summary = "Actualizar usuario", description = "Actualiza la información de un usuario existente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuario actualizado correctamente",
                    content = @Content(schema = @Schema(implementation = UserDTO.UserResponse.class))),
            @ApiResponse(responseCode = "400", description = "Datos de actualización inválidos", content = @Content),
            @ApiResponse(responseCode = "403", description = "No tiene permisos para actualizar este usuario", content = @Content),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado", content = @Content)
    })
    public ResponseEntity<UserDTO.UserResponse> updateUser(
            @Parameter(description = "ID del usuario a actualizar", required = true)
            @PathVariable Long userId,
            @Parameter(description = "Datos de actualización del usuario", required = true)
            @RequestBody @Valid UserDTO.UserUpdateRequest request) {
        return ResponseEntity.ok(userService.updateUser(userId, request));
    }

    @PostMapping(value = "/{userId}/profile-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Subir imagen de perfil", description = "Sube una imagen de perfil para un usuario")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Imagen subida correctamente",
                    content = @Content(schema = @Schema(implementation = UserDTO.UserResponse.class))),
            @ApiResponse(responseCode = "400", description = "Archivo inválido", content = @Content),
            @ApiResponse(responseCode = "403", description = "No tiene permisos para actualizar este usuario", content = @Content),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado", content = @Content)
    })
    public ResponseEntity<UserDTO.UserResponse> uploadProfileImage(
            @Parameter(description = "ID del usuario", required = true)
            @PathVariable Long userId,
            @Parameter(description = "Archivo de imagen", required = true)
            @RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(userService.uploadProfileImage(userId, file));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Listar todos los usuarios", description = "Obtiene la lista de todos los usuarios (solo para administradores)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de usuarios obtenida correctamente"),
            @ApiResponse(responseCode = "403", description = "No tiene permisos de administrador", content = @Content)
    })
    public ResponseEntity<List<UserDTO.UserResponse>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @PutMapping("/{userId}/roles")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Actualizar roles de usuario", description = "Actualiza los roles asignados a un usuario (solo para administradores)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Roles actualizados correctamente",
                    content = @Content(schema = @Schema(implementation = UserDTO.UserResponse.class))),
            @ApiResponse(responseCode = "400", description = "Datos de roles inválidos", content = @Content),
            @ApiResponse(responseCode = "403", description = "No tiene permisos de administrador", content = @Content),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado", content = @Content)
    })
    public ResponseEntity<UserDTO.UserResponse> updateUserRoles(
            @Parameter(description = "ID del usuario", required = true)
            @PathVariable Long userId,
            @Parameter(description = "Roles a asignar", required = true)
            @RequestBody UserDTO.UserRolesUpdateRequest request) {
        return ResponseEntity.ok(userService.updateUserRoles(userId, request.getRoles()));
    }

    @PutMapping("/{userId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Actualizar estado de usuario", description = "Actualiza el estado activo de un usuario (solo para administradores)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Estado actualizado correctamente",
                    content = @Content(schema = @Schema(implementation = UserDTO.UserResponse.class))),
            @ApiResponse(responseCode = "403", description = "No tiene permisos de administrador", content = @Content),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado", content = @Content)
    })
    public ResponseEntity<UserDTO.UserResponse> updateUserStatus(
            @Parameter(description = "ID del usuario", required = true)
            @PathVariable Long userId,
            @Parameter(description = "Estado a asignar", required = true)
            @RequestBody UserDTO.UserStatusUpdateRequest request) {
        return ResponseEntity.ok(userService.updateUserStatus(userId, request.getIsActive()));
    }

    @DeleteMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Eliminar usuario", description = "Elimina un usuario por su ID (solo para administradores)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Usuario eliminado correctamente"),
            @ApiResponse(responseCode = "403", description = "No tiene permisos de administrador", content = @Content),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado", content = @Content)
    })
    public ResponseEntity<Void> deleteUser(
            @Parameter(description = "ID del usuario a eliminar", required = true)
            @PathVariable Long userId) {
        userService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }
}