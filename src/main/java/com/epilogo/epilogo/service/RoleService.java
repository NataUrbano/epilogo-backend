package com.epilogo.epilogo.service;

import com.epilogo.epilogo.dto.RoleDTO;
import com.epilogo.epilogo.exception.ResourceNotFoundException;
import com.epilogo.epilogo.model.Role;
import com.epilogo.epilogo.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoleService {

    private final RoleRepository roleRepository;

    /**
     * Obtener todos los roles disponibles
     */
    public List<RoleDTO.RoleResponse> getAllRoles() {
        return roleRepository.findAll().stream()
                .map(this::mapToRoleResponse)
                .collect(Collectors.toList());
    }

    /**
     * Obtener un rol por su ID
     */
    public RoleDTO.RoleResponse getRoleById(Long roleId) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Rol no encontrado con ID: " + roleId));
        return mapToRoleResponse(role);
    }

    /**
     * Obtener un rol por su nombre
     */
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

    /**
     * Obtener todos los nombres de roles como enum
     */
    public List<String> getAllRoleNames() {
        return Arrays.stream(Role.RoleName.values())
                .map(Enum::name)
                .collect(Collectors.toList());
    }

    /**
     * Metodo interno para mapear entidad Role a RoleResponse DTO
     */
    private RoleDTO.RoleResponse mapToRoleResponse(Role role) {
        return RoleDTO.RoleResponse.builder()
                .roleId(role.getRoleId())
                .roleName(role.getRoleName().name())
                .build();
    }
}