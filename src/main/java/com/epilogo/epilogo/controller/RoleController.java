package com.epilogo.epilogo.controller;

import com.epilogo.epilogo.dto.RoleDTO;
import com.epilogo.epilogo.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    /**
     * Obtener todos los roles
     * Solo accesible para administradores
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<RoleDTO.RoleResponse>> getAllRoles() {
        return ResponseEntity.ok(roleService.getAllRoles());
    }

    /**
     * Obtener un rol por ID
     * Solo accesible para administradores
     */
    @GetMapping("/{roleId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RoleDTO.RoleResponse> getRoleById(@PathVariable Long roleId) {
        return ResponseEntity.ok(roleService.getRoleById(roleId));
    }

    /**
     * Obtener un rol por nombre
     * Solo accesible para administradores
     */
    @GetMapping("/name/{roleName}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RoleDTO.RoleResponse> getRoleByName(@PathVariable String roleName) {
        return ResponseEntity.ok(roleService.getRoleByName(roleName));
    }

    /**
     * Obtener todos los nombres de roles disponibles
     * Solo accesible para administradores
     */
    @GetMapping("/names")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<String>> getAllRoleNames() {
        return ResponseEntity.ok(roleService.getAllRoleNames());
    }
}