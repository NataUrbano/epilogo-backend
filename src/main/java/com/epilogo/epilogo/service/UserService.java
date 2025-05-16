package com.epilogo.epilogo.service;

import com.epilogo.epilogo.dto.S3FileDTO;
import com.epilogo.epilogo.dto.UserDTO;
import com.epilogo.epilogo.exception.ResourceNotFoundException;
import com.epilogo.epilogo.model.Role;
import com.epilogo.epilogo.model.S3File;
import com.epilogo.epilogo.model.User;
import com.epilogo.epilogo.repository.RoleRepository;
import com.epilogo.epilogo.repository.S3FileRepository;
import com.epilogo.epilogo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final S3Service s3Service;
    private final S3FileRepository s3FileRepository;

    /**
     * Get current authenticated user
     */
    public UserDTO.UserResponse getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        return mapToUserResponse(user);
    }

    /**
     * Get user by ID
     */
    public UserDTO.UserResponse getUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + userId));

        return mapToUserResponse(user);
    }

    /**
     * Update user profile
     */
    @Transactional
    public UserDTO.UserResponse updateUser(Long userId, UserDTO.UserUpdateRequest request) {
        // Check if user is authorized to update this profile
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        // Only allow users to update their own profile unless they're an admin
        if (!currentUser.getUserId().equals(userId) &&
                !currentUser.getRoles().stream().anyMatch(role -> role.getRoleName().name().equals("ROLE_ADMIN"))) {
            throw new AccessDeniedException("No está autorizado para modificar este perfil");
        }

        // Get user to update
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + userId));

        // Update user name if provided
        if (request.getUserName() != null && !request.getUserName().isBlank()) {
            user.setUserName(request.getUserName());
        }

        // Update password if provided
        if (request.getNewPassword() != null && !request.getNewPassword().isBlank()) {
            // Verify current password if not admin
            if (!currentUser.getRoles().stream().anyMatch(role -> role.getRoleName().name().equals("ROLE_ADMIN"))) {
                if (request.getCurrentPassword() == null || !passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
                    throw new AccessDeniedException("Contraseña actual incorrecta");
                }
            }

            user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        }

        // Save updated user
        User updatedUser = userRepository.save(user);

        return mapToUserResponse(updatedUser);
    }

    /**
     * Upload user profile image
     */
    @Transactional
    public UserDTO.UserResponse uploadProfileImage(Long userId, MultipartFile file) {
        // Check if user is authorized to update this profile
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        // Only allow users to update their own profile unless they're an admin
        if (!currentUser.getUserId().equals(userId) &&
                !currentUser.getRoles().stream().anyMatch(role -> role.getRoleName().name().equals("ROLE_ADMIN"))) {
            throw new AccessDeniedException("No está autorizado para modificar este perfil");
        }

        // Get user to update
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + userId));

        // Upload file to S3
        S3File s3File = s3Service.uploadFile(file, S3File.EntityType.USER, user.getUserId());

        // Return updated user
        user.setImageUrl(s3File.getS3Url());
        System.out.println(user.getImageUrl());
        User updatedUser = userRepository.save(user);

        return mapToUserResponse(updatedUser);
    }

    /**
     * Get all users (admin only)
     */
    public List<UserDTO.UserResponse> getAllUsers() {
        return userRepository.findAllWithRoles().stream()
                .map(this::mapToUserResponse)
                .collect(Collectors.toList());
    }

    /**
     * Actualizar los roles de un usuario (solo para administradores)
     */
    @Transactional
    public UserDTO.UserResponse updateUserRoles(Long userId, List<String> roleNames) {
        // Verificar si el usuario existe
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + userId));

        // Limpiar roles actuales
        user.getRoles().clear();

        // Añadir nuevos roles
        for (String roleName : roleNames) {
            try {
                Role.RoleName enumRoleName = Role.RoleName.valueOf(roleName);
                Role role = roleRepository.findByRoleName(enumRoleName)
                        .orElseThrow(() -> new ResourceNotFoundException("Rol no encontrado: " + roleName));
                user.getRoles().add(role);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Nombre de rol inválido: " + roleName);
            }
        }

        // Guardar cambios
        User updatedUser = userRepository.save(user);

        return mapToUserResponse(updatedUser);
    }

    /**
     * Actualizar el estado de un usuario (activar/desactivar)
     */
    @Transactional
    public UserDTO.UserResponse updateUserStatus(Long userId, Boolean isActive) {
        // Verificar si el usuario existe
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + userId));

        // Actualizar estado
        user.setActive(isActive);

        // Guardar cambios
        User updatedUser = userRepository.save(user);

        return mapToUserResponse(updatedUser);
    }

    /**
     * Eliminar un usuario
     */
    @Transactional
    public void deleteUser(Long userId) {
        // Verificar si el usuario existe
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("Usuario no encontrado con ID: " + userId);
        }

        // Eliminar usuario
        userRepository.deleteById(userId);
    }

    /**
     * Helper method to map User entity to UserResponse DTO
     */
    private UserDTO.UserResponse mapToUserResponse(User user) {
        Optional<S3File> latestFileOpt = s3FileRepository.findFirstByEntityIdAndEntityTypeOrderByUploadDateDesc(user.getUserId(), S3File.EntityType.USER);

        String imageUrl = latestFileOpt.map(S3File::getS3Url).orElse(null);

        return UserDTO.UserResponse.builder()
                .userId(user.getUserId())
                .userName(user.getUserName())
                .email(user.getEmail())
                .imageUrl(imageUrl)
                .registerDate(user.getRegisterDate())
                .isActive(user.getActive())
                .roles(user.getRoles().stream()
                        .map(role -> role.getRoleName().name())
                        .collect(Collectors.toSet()))
                .build();
    }

}