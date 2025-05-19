package com.epilogo.epilogo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Set;

@Schema(description = "DTOs relacionados con la autenticación y autorización")
public class AuthDTO {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(name = "AuthenticationResponse", description = "Respuesta con los datos de autenticación exitosa")
    public static class AuthenticationResponse {
        @Schema(description = "Token de acceso JWT", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
        private String accessToken;

        @Schema(description = "Token de refresco JWT", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
        private String refreshToken;

        @Schema(description = "ID del usuario autenticado", example = "42")
        private Long userId;

        @Schema(description = "Nombre de usuario", example = "juanperez")
        private String userName;

        @Schema(description = "Correo electrónico del usuario", example = "juan@example.com")
        private String email;

        @Schema(description = "URL de la imagen de perfil del usuario", example = "https://epilogo-bucket.s3.amazonaws.com/users/profile42.jpg")
        private String imageUrl;

        @Schema(description = "Roles asignados al usuario", example = "[\"ADMIN\", \"USER\"]")
        private Set<String> roles;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(name = "RefreshTokenRequest", description = "Solicitud para refrescar el token de acceso")
    public static class RefreshTokenRequest {
        @Schema(description = "Token de refresco JWT", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...", required = true)
        private String refreshToken;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(name = "TokenResponse", description = "Respuesta con los nuevos tokens")
    public static class TokenResponse {
        @Schema(description = "Nuevo token de acceso JWT", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
        private String accessToken;

        @Schema(description = "Token de refresco JWT (puede ser el mismo o uno nuevo)", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
        private String refreshToken;
    }
}