package com.epilogo.epilogo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

public class RoleDTO {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(name = "RoleResponse", description = "Respuesta con la información de un rol")
    public static class RoleResponse {
        @Schema(description = "Identificador único del rol", example = "1")
        private Long roleId;

        @Schema(description = "Nombre del rol", example = "ADMIN")
        private String roleName;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(name = "RolesResponse", description = "Respuesta con la lista de roles")
    public static class RolesResponse {
        @Schema(description = "Lista de roles")
        private List<RoleResponse> roles;
    }
}
