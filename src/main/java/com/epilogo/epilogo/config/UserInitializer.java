package com.epilogo.epilogo.config;

import com.epilogo.epilogo.model.Role;
import com.epilogo.epilogo.model.User;
import com.epilogo.epilogo.repository.RoleRepository;
import com.epilogo.epilogo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import io.swagger.v3.oas.annotations.Hidden;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Hidden
public class UserInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        createRolesIfNotExist();

        if (userRepository.count() == 0) {
            createDefaultUsers();
        }
    }

    private void createRolesIfNotExist() {
        Arrays.stream(Role.RoleName.values())
                .forEach(roleName -> {
                    if (!roleRepository.existsByRoleName(roleName)) {
                        Role role = new Role();
                        role.setRoleName(roleName);
                        roleRepository.save(role);
                        System.out.println("Rol creado: " + roleName);
                    }
                });
    }

    private void createDefaultUsers() {
        Role adminRole = roleRepository.findByRoleName(Role.RoleName.ROLE_ADMIN)
                .orElseThrow(() -> new RuntimeException("Rol ADMIN no encontrado"));

        Role librarianRole = roleRepository.findByRoleName(Role.RoleName.ROLE_LIBRARIAN)
                .orElseThrow(() -> new RuntimeException("Rol LIBRARIAN no encontrado"));

        Role userRole = roleRepository.findByRoleName(Role.RoleName.ROLE_USER)
                .orElseThrow(() -> new RuntimeException("Rol USER no encontrado"));

        List<User> defaultUsers = Arrays.asList(
                createUser("Admin", "admin@epilogo.com", "Admin123*", Set.of(adminRole, librarianRole, userRole)),
                createUser("Natalia Admin", "natalia@admin.com", "Admin123*", Set.of(adminRole, userRole)),
                createUser("Natalia Librarian", "natalia@librarian.com", "Admin123*", Set.of(librarianRole, userRole))
        );

        userRepository.saveAll(defaultUsers);

        System.out.println("Usuarios predeterminados creados con éxito.");
    }

    private User createUser(String userName, String email, String password, Set<Role> roles) {
        User user = new User();
        user.setUserName(userName);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setRegisterDate(LocalDateTime.now());
        user.setActive(true);
        user.setRoles(new HashSet<>(roles));
        return user;
    }
}