package com.epilogo.epilogo.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import io.swagger.v3.oas.annotations.media.Schema;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "roles")
@Schema(description = "Entidad que representa un rol en el sistema")
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "role_id")
    @Schema(description = "Identificador único del rol", example = "1")
    private Long roleId;

    @Column(name = "role_name", nullable = false, unique = true, length = 50)
    @Enumerated(EnumType.STRING)
    @Schema(description = "Nombre del rol", example = "ROLE_ADMIN", required = true)
    private RoleName roleName;

    @Schema(description = "Tipos de roles disponibles en el sistema")
    public enum RoleName {
        @Schema(description = "Rol de usuario normal")
        ROLE_USER,
        @Schema(description = "Rol de administrador con acceso total")
        ROLE_ADMIN,
        @Schema(description = "Rol de bibliotecario con acceso a gestión de libros y préstamos")
        ROLE_LIBRARIAN
    }

    @Override
    public String toString() {
        return "Role{" +
                "roleId=" + roleId +
                ", roleName='" + roleName + '\'' +
                '}';
    }
}