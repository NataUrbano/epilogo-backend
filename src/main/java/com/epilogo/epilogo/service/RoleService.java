package com.epilogo.epilogo.service;

import com.epilogo.epilogo.dto.RoleDTO;
import com.epilogo.epilogo.exception.ResourceNotFoundException;
import com.epilogo.epilogo.model.Role;
import com.epilogo.epilogo.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Tag(name = "Role Service", description = "Servicio para gestión de roles")
public class RoleService {

    private final RoleRepository roleRepository;

    @Operation(summary = "Obtener todos los roles", description = "Devuelve la lista de todos los roles disponibles en el sistema")
    public List<RoleDTO.RoleResponse> getAllRoles() {
        return roleRepository.findAll().stream()
                .map(this::mapToRoleResponse)
                .collect(Collectors.toList());
    }

    @Operation(summary = "Obtener rol por ID", description = "Busca un rol por su ID único")
    public RoleDTO.RoleResponse getRoleById(Long roleId) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Rol no encontrado con ID: " + roleId));
        return mapToRoleResponse(role);
    }

    @Operation(summary = "Obtener rol por nombre", description = "Busca un rol por su nombre")
    public RoleDTO.RoleResponse getRoleByName(String roleName) {
        try {
            Role.RoleName enumRoleName = Role.RoleName.valueOf(roleName.toUpperCase());
            Role role = roleRepository.findByRoleName(enumRoleName)
                    .orElseThrow(() -> new ResourceNotFoundException("Rol no encontrado con nombre: " + roleName));
            return mapToRoleResponse(role);
        } catch (IllegalArgumentException e) {
            throw new ResourceNotFoundException("Nombre de rol inválido: " + roleName);
        }
    }

    @Operation(summary = "Obtener nombres de roles", description = "Devuelve la lista de todos los nombres de roles disponibles")
    public List<String> getAllRoleNames() {
        return Arrays.stream(Role.RoleName.values())
                .map(Enum::name)
                .collect(Collectors.toList());
    }

    private RoleDTO.RoleResponse mapToRoleResponse(Role role) {
        return RoleDTO.RoleResponse.builder()
                .roleId(role.getRoleId())
                .roleName(role.getRoleName().name())
                .build();
    }
}