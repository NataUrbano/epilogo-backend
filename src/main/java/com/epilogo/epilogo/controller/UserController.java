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

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<UserDTO.UserResponse> getCurrentUser() {
        return ResponseEntity.ok(userService.getCurrentUser());
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserDTO.UserResponse> getUserById(@PathVariable Long userId) {
        return ResponseEntity.ok(userService.getUserById(userId));
    }

    @PutMapping("/{userId}")
    public ResponseEntity<UserDTO.UserResponse> updateUser(
            @PathVariable Long userId,
            @RequestBody @Valid UserDTO.UserUpdateRequest request) {
        return ResponseEntity.ok(userService.updateUser(userId, request));
    }

    @PostMapping(value = "/{userId}/profile-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UserDTO.UserResponse> uploadProfileImage(
            @PathVariable Long userId,
            @RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(userService.uploadProfileImage(userId, file));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserDTO.UserResponse>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    /**
     * Actualizar los roles de un usuario (solo para administradores)
     */
    @PutMapping("/{userId}/roles")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDTO.UserResponse> updateUserRoles(
            @PathVariable Long userId,
            @RequestBody UserDTO.UserRolesUpdateRequest request) {
        return ResponseEntity.ok(userService.updateUserRoles(userId, request.getRoles()));
    }

    /**
     * Actualizar el estado de un usuario (activar/desactivar)
     */
    @PutMapping("/{userId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDTO.UserResponse> updateUserStatus(
            @PathVariable Long userId,
            @RequestBody UserDTO.UserStatusUpdateRequest request) {
        return ResponseEntity.ok(userService.updateUserStatus(userId, request.getIsActive()));
    }

    /**
     * Eliminar un usuario
     */
    @DeleteMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable Long userId) {
        userService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }
}