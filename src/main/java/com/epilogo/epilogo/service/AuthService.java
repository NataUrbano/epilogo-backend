package com.epilogo.epilogo.service;

import com.epilogo.epilogo.dto.AuthDTO;
import com.epilogo.epilogo.dto.UserDTO;
import com.epilogo.epilogo.exception.UserAlreadyExistsException;
import com.epilogo.epilogo.model.Role;
import com.epilogo.epilogo.model.User;
import com.epilogo.epilogo.repository.RoleRepository;
import com.epilogo.epilogo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Tag(name = "Auth Service", description = "Servicio para gestionar la autenticación de usuarios")
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;

    @Transactional
    @Operation(summary = "Registrar usuario", description = "Registra un nuevo usuario en el sistema")
    public AuthDTO.AuthenticationResponse register(UserDTO.UserRegistrationRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException("El correo electrónico ya está registrado");
        }

        Role userRole = roleRepository.findByRoleName(Role.RoleName.ROLE_USER)
                .orElseThrow(() -> new RuntimeException("El rol de usuario no existe"));

        User user = User.builder()
                .userName(request.getUserName())
                .email(request.getEmail().toLowerCase().trim())
                .password(passwordEncoder.encode(request.getPassword()))
                .roles(new HashSet<>(Set.of(userRole)))
                .isActive(true)
                .build();

        User savedUser = userRepository.save(user);

        UserDetails userDetails = userDetailsService.loadUserByUsername(savedUser.getEmail());
        String accessToken = jwtService.generateToken(userDetails);
        String refreshToken = jwtService.generateRefreshToken(userDetails);

        return AuthDTO.AuthenticationResponse.builder()
                .userId(savedUser.getUserId())
                .userName(savedUser.getUserName())
                .email(savedUser.getEmail())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    @Operation(summary = "Autenticar usuario", description = "Autentica un usuario y devuelve tokens JWT")
    public AuthDTO.AuthenticationResponse authenticate(UserDTO.UserLoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        String accessToken = jwtService.generateToken(userDetails);
        String refreshToken = jwtService.generateRefreshToken(userDetails);
        Set<String> roles = user.getRoles().stream()
                .map(role -> role.getRoleName().name())
                .collect(Collectors.toSet());

        return AuthDTO.AuthenticationResponse.builder()
                .userId(user.getUserId())
                .userName(user.getUserName())
                .email(user.getEmail())
                .imageUrl(user.getImageUrl())
                .roles(roles)
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    @Operation(summary = "Refrescar token", description = "Refresca el token de acceso usando un token de refresco válido")
    public AuthDTO.TokenResponse refreshToken(AuthDTO.RefreshTokenRequest request) {
        String username = jwtService.extractUsername(request.getRefreshToken());

        if (username != null) {
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            if (jwtService.isTokenValid(request.getRefreshToken(), userDetails)) {
                String accessToken = jwtService.generateToken(userDetails);

                return AuthDTO.TokenResponse.builder()
                        .accessToken(accessToken)
                        .refreshToken(request.getRefreshToken())
                        .build();
            }
        }

        throw new RuntimeException("Refresh token inválido");
    }
}