package com.epilogo.epilogo.service;

import com.epilogo.epilogo.dto.AuthDTO;
import com.epilogo.epilogo.dto.UserDTO;
import com.epilogo.epilogo.exception.UserAlreadyExistsException;
import com.epilogo.epilogo.model.Role;
import com.epilogo.epilogo.model.User;
import com.epilogo.epilogo.repository.RoleRepository;
import com.epilogo.epilogo.repository.UserRepository;
import com.epilogo.epilogo.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;

    /**
     * Register a new user
     */
    @Transactional
    public AuthDTO.AuthenticationResponse register(UserDTO.UserRegistrationRequest request) {
        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException("El correo electrónico ya está registrado");
        }

        // Get default user role
        Role userRole = roleRepository.findByRoleName(Role.RoleName.ROLE_USER)
                .orElseThrow(() -> new RuntimeException("El rol de usuario no existe"));

        // Create new user
        User user = User.builder()
                .userName(request.getUserName())
                .email(request.getEmail().toLowerCase().trim())
                .password(passwordEncoder.encode(request.getPassword()))
                .roles(new HashSet<>(Set.of(userRole)))
                .isActive(true)
                .build();

        // Save user
        User savedUser = userRepository.save(user);

        // Generate tokens
        UserDetails userDetails = userDetailsService.loadUserByUsername(savedUser.getEmail());
        String accessToken = jwtService.generateToken(userDetails);
        String refreshToken = jwtService.generateRefreshToken(userDetails);

        // Return response
        return AuthDTO.AuthenticationResponse.builder()
                .userId(savedUser.getUserId())
                .userName(savedUser.getUserName())
                .email(savedUser.getEmail())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    /**
     * Authenticate a user
     */
    public AuthDTO.AuthenticationResponse authenticate(UserDTO.UserLoginRequest request) {
        // Authenticate
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        // User is authenticated at this point
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Generate tokens
        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        String accessToken = jwtService.generateToken(userDetails);
        String refreshToken = jwtService.generateRefreshToken(userDetails);
        Set<String> roles = user.getRoles().stream()
                .map(role -> role.getRoleName().name())
                .collect(Collectors.toSet());
        System.out.println(roles);
        // Return response
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

    /**
     * Refresh authentication token
     */
    public AuthDTO.TokenResponse refreshToken(AuthDTO.RefreshTokenRequest request) {
        // Extract username from refresh token
        String username = jwtService.extractUsername(request.getRefreshToken());

        if (username != null) {
            // Load user details
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            // Validate refresh token
            if (jwtService.isTokenValid(request.getRefreshToken(), userDetails)) {
                // Generate new access token
                String accessToken = jwtService.generateToken(userDetails);

                // Return response
                return AuthDTO.TokenResponse.builder()
                        .accessToken(accessToken)
                        .refreshToken(request.getRefreshToken())
                        .build();
            }
        }

        throw new RuntimeException("Refresh token inválido");
    }
}