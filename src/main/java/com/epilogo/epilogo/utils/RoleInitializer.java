package com.epilogo.epilogo.utils;

import com.epilogo.epilogo.model.Role;
import com.epilogo.epilogo.repository.RoleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class RoleInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;

    public RoleInitializer(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        for (Role.RoleName roleName : Role.RoleName.values()) {
            if (!roleRepository.existsByRoleName(roleName)) {
                Role role = Role.builder()
                        .roleName(roleName)
                        .build();
                roleRepository.save(role);
                System.out.println("Rol creado: " + roleName);
            }
        }
    }
}