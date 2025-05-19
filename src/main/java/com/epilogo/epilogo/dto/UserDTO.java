package com.epilogo.epilogo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

public class UserDTO {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(name = "UserResponse", description = "Respuesta con los datos completos de un usuario")
    public static class UserResponse {
        @Schema(description = "Identificador único del usuario", example = "123", accessMode = Schema.AccessMode.READ_ONLY)
        private Long userId;

        @Schema(description = "Nombre de usuario", example = "juanperez")
        private String userName;

        @Schema(description = "Correo electrónico del usuario", example = "usuario@correo.com", format = "email")
        private String email;

        @Schema(description = "URL de la imagen del usuario", example = "https://example.com/images/user123.png")
        private String imageUrl;

        @Schema(description = "Fecha de registro del usuario", example = "2024-05-18T16:50:00", format = "date-time", accessMode = Schema.AccessMode.READ_ONLY)
        private LocalDateTime registerDate;

        @Schema(description = "Indica si el usuario está activo", example = "true")
        private Boolean isActive;

        @Schema(description = "Roles asignados al usuario", example = "[\"ADMIN\", \"USER\"]")
        private Set<String> roles;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(name = "UserRegistrationRequest", description = "Datos requeridos para registrar un nuevo usuario")
    public static class UserRegistrationRequest {
        @NotBlank(message = "El nombre de usuario es obligatorio")
        @Size(min = 3, max = 150, message = "El nombre debe tener entre 3 y 150 caracteres")
        @Schema(description = "Nombre de usuario", example = "juanperez", required = true)
        private String userName;

        @NotBlank(message = "El email es obligatorio")
        @Email(message = "Formato de email inválido")
        @Schema(description = "Correo electrónico válido", example = "usuario@correo.com", required = true, format = "email")
        private String email;

        @NotBlank(message = "La contraseña es obligatoria")
        @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres")
        @Schema(description = "Contraseña segura para el usuario", example = "P@ssw0rd", required = true, accessMode = Schema.AccessMode.WRITE_ONLY)
        private String password;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(name = "UserUpdateRequest", description = "Datos para actualizar información de un usuario")
    public static class UserUpdateRequest {
        @Size(min = 3, max = 150, message = "El nombre debe tener entre 3 y 150 caracteres")
        @Schema(description = "Nuevo nombre de usuario", example = "juanperez")
        private String userName;

        @Schema(description = "Contraseña actual para validar cambios", example = "P@ssw0rd", accessMode = Schema.AccessMode.WRITE_ONLY)
        private String currentPassword;

        @Size(min = 6, message = "La nueva contraseña debe tener al menos 6 caracteres")
        @Schema(description = "Nueva contraseña para el usuario", example = "Nuev@P4ss", accessMode = Schema.AccessMode.WRITE_ONLY)
        private String newPassword;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(name = "UserSummary", description = "Resumen básico de un usuario")
    public static class UserSummary {
        @Schema(description = "Identificador único del usuario", example = "123")
        private Long userId;

        @Schema(description = "Nombre de usuario", example = "juanperez")
        private String userName;

        @Schema(description = "URL de la imagen del usuario", example = "https://example.com/images/user123.png")
        private String imageUrl;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(name = "UserLoginRequest", description = "Datos para iniciar sesión")
    public static class UserLoginRequest {
        @NotBlank(message = "El email es obligatorio")
        @Email(message = "Formato de email inválido")
        @Schema(description = "Correo electrónico para login", example = "usuario@correo.com", required = true, format = "email")
        private String email;

        @NotBlank(message = "La contraseña es obligatoria")
        @Schema(description = "Contraseña para login", example = "P@ssw0rd", required = true, accessMode = Schema.AccessMode.WRITE_ONLY)
        private String password;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(name = "UserRolesUpdateRequest", description = "Solicitud para actualizar los roles asignados a un usuario")
    public static class UserRolesUpdateRequest {
        @Schema(description = "Lista de roles a asignar", example = "[\"ADMIN\", \"USER\"]", required = true)
        private List<String> roles;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(name = "UserStatusUpdateRequest", description = "Solicitud para actualizar el estado activo de un usuario")
    public static class UserStatusUpdateRequest {
        @Schema(description = "Indica si el usuario está activo", example = "true", required = true)
        private Boolean isActive;
    }
}
