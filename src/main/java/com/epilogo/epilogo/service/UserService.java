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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Tag(name = "User Service", description = "Servicio para gestionar usuarios")
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final S3Service s3Service;
    private final S3FileRepository s3FileRepository;

    @Operation(summary = "Obtener usuario actual", description = "Obtiene los datos del usuario autenticado actual")
    public UserDTO.UserResponse getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        return mapToUserResponse(user);
    }

    @Operation(summary = "Obtener usuario por ID", description = "Obtiene los datos de un usuario por su ID")
    public UserDTO.UserResponse getUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + userId));

        return mapToUserResponse(user);
    }

    @Transactional
    @Operation(summary = "Actualizar usuario", description = "Actualiza los datos de un usuario existente")
    public UserDTO.UserResponse updateUser(Long userId, UserDTO.UserUpdateRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        if (!currentUser.getUserId().equals(userId) &&
                !currentUser.getRoles().stream().anyMatch(role -> role.getRoleName().name().equals("ROLE_ADMIN"))) {
            throw new AccessDeniedException("No está autorizado para modificar este perfil");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + userId));

        if (request.getUserName() != null && !request.getUserName().isBlank()) {
            user.setUserName(request.getUserName());
        }

        if (request.getNewPassword() != null && !request.getNewPassword().isBlank()) {
            if (!currentUser.getRoles().stream().anyMatch(role -> role.getRoleName().name().equals("ROLE_ADMIN"))) {
                if (request.getCurrentPassword() == null || !passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
                    throw new AccessDeniedException("Contraseña actual incorrecta");
                }
            }

            user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        }

        User updatedUser = userRepository.save(user);

        return mapToUserResponse(updatedUser);
    }

    @Transactional
    @Operation(summary = "Subir imagen de perfil", description = "Sube una imagen de perfil para un usuario")
    public UserDTO.UserResponse uploadProfileImage(Long userId, MultipartFile file) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        if (!currentUser.getUserId().equals(userId) &&
                !currentUser.getRoles().stream().anyMatch(role -> role.getRoleName().name().equals("ROLE_ADMIN"))) {
            throw new AccessDeniedException("No está autorizado para modificar este perfil");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + userId));

        S3File s3File = s3Service.uploadFile(file, S3File.EntityType.USER, user.getUserId());

        user.setImageUrl(s3File.getS3Url());
        User updatedUser = userRepository.save(user);

        return mapToUserResponse(updatedUser);
    }

    @Operation(summary = "Obtener todos los usuarios", description = "Obtiene la lista de todos los usuarios (solo para administradores)")
    public List<UserDTO.UserResponse> getAllUsers() {
        return userRepository.findAllWithRoles().stream()
                .map(this::mapToUserResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    @Operation(summary = "Actualizar roles de usuario", description = "Actualiza los roles asignados a un usuario (solo para administradores)")
    public UserDTO.UserResponse updateUserRoles(Long userId, List<String> roleNames) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + userId));

        user.getRoles().clear();

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

        User updatedUser = userRepository.save(user);

        return mapToUserResponse(updatedUser);
    }

    @Transactional
    @Operation(summary = "Actualizar estado de usuario", description = "Actualiza el estado de activación de un usuario (solo para administradores)")
    public UserDTO.UserResponse updateUserStatus(Long userId, Boolean isActive) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + userId));

        user.setActive(isActive);

        User updatedUser = userRepository.save(user);

        return mapToUserResponse(updatedUser);
    }

    @Transactional
    @Operation(summary = "Eliminar usuario", description = "Elimina un usuario por su ID (solo para administradores)")
    public void deleteUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("Usuario no encontrado con ID: " + userId);
        }

        userRepository.deleteById(userId);
    }

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