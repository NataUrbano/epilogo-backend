package com.epilogo.epilogo.controller;

import com.epilogo.epilogo.dto.AuthDTO;
import com.epilogo.epilogo.dto.UserDTO;
import com.epilogo.epilogo.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticación", description = "API para registro, inicio de sesión y gestión de tokens")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @Operation(summary = "Registrar usuario", description = "Registra un nuevo usuario en el sistema")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuario registrado correctamente",
                    content = @Content(schema = @Schema(implementation = AuthDTO.AuthenticationResponse.class))),
            @ApiResponse(responseCode = "400", description = "Datos de registro inválidos", content = @Content),
            @ApiResponse(responseCode = "409", description = "El correo electrónico ya está registrado", content = @Content)
    })
    public ResponseEntity<AuthDTO.AuthenticationResponse> register(
            @Parameter(description = "Datos de registro de usuario", required = true)
            @RequestBody @Valid UserDTO.UserRegistrationRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    @Operation(summary = "Iniciar sesión", description = "Autentica un usuario y devuelve tokens JWT")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Inicio de sesión exitoso",
                    content = @Content(schema = @Schema(implementation = AuthDTO.AuthenticationResponse.class))),
            @ApiResponse(responseCode = "400", description = "Datos de inicio de sesión inválidos", content = @Content),
            @ApiResponse(responseCode = "401", description = "Credenciales incorrectas", content = @Content)
    })
    public ResponseEntity<AuthDTO.AuthenticationResponse> login(
            @Parameter(description = "Credenciales de inicio de sesión", required = true)
            @RequestBody @Valid UserDTO.UserLoginRequest request) {
        return ResponseEntity.ok(authService.authenticate(request));
    }

    @PostMapping("/refresh-token")
    @Operation(summary = "Refrescar token", description = "Refresca el token de acceso usando un token de refresco válido")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Token refrescado correctamente",
                    content = @Content(schema = @Schema(implementation = AuthDTO.TokenResponse.class))),
            @ApiResponse(responseCode = "400", description = "Token de refresco inválido", content = @Content),
            @ApiResponse(responseCode = "401", description = "Token de refresco expirado o no válido", content = @Content)
    })
    public ResponseEntity<AuthDTO.TokenResponse> refreshToken(
            @Parameter(description = "Token de refresco", required = true)
            @RequestBody AuthDTO.RefreshTokenRequest request) {
        return ResponseEntity.ok(authService.refreshToken(request));
    }
}